package com.casualgames.securitystarter.jwt.filter;

import com.casualgames.securitystarter.config.AuthenticationToken;
import com.casualgames.securitystarter.jwt.JwtClaimsExtractor;
import com.casualgames.securitystarter.jwt.TokenClaims;
import com.casualgames.securitystarter.provider.PermissionProvider;
import com.casualgames.securitystarter.validator.JwtValidator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtValidator jwtValidator;

    private final JwtClaimsExtractor claimsExtractor;

    private final PermissionProvider permissionProvider;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String jwt = extractJwt(request);

        if (jwt != null) {
            try {
                if (jwtValidator.isValid(jwt)) {
                    AuthenticationToken authentication = createAuthentication(jwt);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                log.error("Error during JWT authentication", e);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractJwt(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        return null;
    }

    private AuthenticationToken createAuthentication(String jwt) {
        TokenClaims claims = claimsExtractor.extractAll(jwt);

        Set<String> permissions = permissionProvider.loadPermissions(claims.roles(), claims.email());

        return AuthenticationToken.authenticated(
                claims.guid(),
                claims.sid(),
                claims.email(),
                claims.status(),
                claims.roles(),
                permissions,
                Map.of()
        );
    }
}
