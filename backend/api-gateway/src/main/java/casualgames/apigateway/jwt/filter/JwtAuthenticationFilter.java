package casualgames.apigateway.jwt.filter;

import casualgames.apigateway.jwt.JwtClaimsExtractor;
import casualgames.apigateway.jwt.JwtProperties;
import casualgames.apigateway.jwt.JwtValidator;
import com.common_utils.dto.ErrorResponse;
import com.common_utils.enums.ErrorCode;
import com.common_utils.enums.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.PathContainer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static casualgames.apigateway.config.ResourceMessageConstants.BAD_REQUEST_AUTHENTICATION_TOKEN;
import static casualgames.apigateway.config.ResourceMessageConstants.BAD_REQUEST_AUTHORIZATION_HEADER;

@Slf4j
public class JwtAuthenticationFilter implements WebFilter, Ordered {

    private static final String BEARER_PREFIX = "Bearer ";
    static final String HEADER_USER_GUID = "X-User-Guid";
    static final String HEADER_USER_EMAIL = "X-User-Email";
    static final String HEADER_USER_ROLE = "X-User-Role";

    private final ObjectMapper objectMapper;
    private final JwtValidator jwtValidator;
    private final JwtClaimsExtractor jwtClaimsExtractor;
    private final List<PathPattern> pathPatterns;

    public JwtAuthenticationFilter(ObjectMapper objectMapper,
                                   JwtProperties jwtProperties,
                                   JwtValidator jwtValidator,
                                   JwtClaimsExtractor jwtClaimsExtractor) {
        this.objectMapper = objectMapper;
        this.jwtValidator = jwtValidator;
        this.jwtClaimsExtractor = jwtClaimsExtractor;
        this.pathPatterns = jwtProperties.publicPaths() == null
                ? List.of()
                : jwtProperties.publicPaths().stream()
                .map(PathPatternParser.defaultInstance::parse)
                .toList();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return unauthorized(exchange, path, BAD_REQUEST_AUTHORIZATION_HEADER);
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        if (!jwtValidator.isValidAndNotExpired(token)) {
            return unauthorized(exchange, path, BAD_REQUEST_AUTHENTICATION_TOKEN);
        }

        try {
            UUID guid = jwtClaimsExtractor.extractGuid(token);
            String email = jwtClaimsExtractor.extractEmail(token);
            Role role = jwtClaimsExtractor.extractRole(token);
            String roleName = role != null ? role.name() : "";

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    guid,
                    null,
                    roleName.isBlank() ? List.of() : List.of(new SimpleGrantedAuthority(roleName))
            );

            ServerHttpRequest request = exchange.getRequest().mutate()
                    .header(HEADER_USER_GUID, guid.toString())
                    .header(HEADER_USER_EMAIL, email != null ? email : "")
                    .header(HEADER_USER_ROLE, roleName)
                    .build();

            return chain.filter(exchange.mutate().request(request).build())
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
        } catch (Exception e) {
            log.warn("JWT claims extraction failed for path={}: {}", path, e.getMessage());

            return unauthorized(exchange, path, ErrorCode.UNAUTHORIZED.getMessage());
        }
    }

    private boolean isPublicPath(String path) {
        if (pathPatterns.isEmpty()) {
            return false;
        }

        PathContainer pathContainer = PathContainer.parsePath(path);

        return pathPatterns.stream()
                .anyMatch(pattern -> pattern.matches(pathContainer));
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String path, String message) {
        try {
            ErrorResponse errorResponse = ErrorResponse.of(
                    ErrorCode.UNAUTHORIZED,
                    HttpStatus.UNAUTHORIZED,
                    message,
                    null,
                    path
            );

            byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);

            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (Exception e) {
            log.error("Failed to write error response", e);

            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);

            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}