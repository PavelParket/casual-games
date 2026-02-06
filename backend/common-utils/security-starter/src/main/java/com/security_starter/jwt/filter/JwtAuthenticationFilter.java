package com.security_starter.jwt.filter;

import com.security_starter.config.AuthenticationToken;
import com.security_starter.enums.Status;
import com.security_starter.exception.JwtException;
import com.security_starter.jwt.JwtClaimsExtractor;
import com.security_starter.provider.PermissionProvider;
import com.security_starter.validator.JwtValidator;
import com.security_starter.whitelist.ServiceWhitelistChecker;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtValidator jwtValidator;

    private final JwtClaimsExtractor claimsExtractor;

    private final PermissionProvider permissionProvider;

    // Temp whitelist
    private final ServiceWhitelistChecker serviceWhitelistChecker;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        // Проверяем whitelist - если запрос от доверенного сервиса, пропускаем без JWT
        if (serviceWhitelistChecker.isWhitelistedService(request)) {
            log.info("Request from whitelisted service: host={}, address={}, port={} - skipping JWT validation", request.getRemoteHost(), request.getRemoteAddr(), request.getRemotePort());

            setServiceToServiceAuthentication();
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = extractJwtFromRequest(request);

            if (jwt != null && jwtValidator.isValidAndNotExpired(jwt)) {
                AuthenticationToken authentication = createAuthentication(jwt);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (JwtException e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            // Don't set authentication, let it continue to AuthenticationEntryPoint
        } catch (Exception e) {
            log.error("Error during JWT authentication", e);
        }

        filterChain.doFilter(request, response);
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        return null;
    }

    private AuthenticationToken createAuthentication(String jwt) {
        UUID guid = claimsExtractor.extractGuid(jwt);
        String email = claimsExtractor.extractEmail(jwt);
        Status status = claimsExtractor.extractStatus(jwt);
        Set<String> roles = claimsExtractor.extractRoles(jwt) != null
                ? claimsExtractor.extractRoles(jwt)
                : Set.of();

        Map<String, Set<String>> roleAndPermissionsMap = permissionProvider.getPermissions(roles, email);
        Set<String> allPermissions = permissionProvider.getAllPermissions(roles, email);

        return AuthenticationToken.authenticated(
                guid,
                email,
                status,
                roles,
                allPermissions,
                roleAndPermissionsMap
        );
    }

    /**
     * Устанавливает специальную аутентификацию для межсервисных запросов из whitelist.
     */
    private void setServiceToServiceAuthentication() {
        AuthenticationToken serviceAuth = new AuthenticationToken(
                null,
                "service-to-service",
                null,
                Set.of(),
                Map.of(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_SERVICE"))
        );

        SecurityContextHolder.getContext().setAuthentication(serviceAuth);
    }

    // Can be extended to skip certain paths
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        return path.startsWith("/actuator/health") || path.startsWith("/actuator/info");
    }
}
