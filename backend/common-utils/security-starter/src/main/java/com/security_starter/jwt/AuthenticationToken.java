package com.security_starter.jwt;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AuthenticationToken extends AbstractAuthenticationToken {

    @Getter
    private Set<String> roles;

    @Getter
    private String status;

    private Object principal;

    private Object credentials;

    @Getter
    private Set<String> permissions;

    @Getter
    private Map<String, Set<String>> roleAndPermissionsMap;

    @Getter
    private String email;

    public AuthenticationToken(
            String status,
            Object principal,
            Set<String> permissions,
            Map<String, Set<String>> roleAndPermissionsMap,
            String email,
            Collection<? extends GrantedAuthority> authorities
    ) {
        super(authorities);
        this.status = status;
        this.principal = principal;
        this.credentials = null;
        this.permissions = permissions;
        this.roleAndPermissionsMap = roleAndPermissionsMap;
        this.email = email;
        if (!authorities.isEmpty()) {
            this.roles = authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
            super.setAuthenticated(true);
        } else {
            super.setAuthenticated(false);
        }
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
        credentials = null;
    }

    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        if (isAuthenticated) {
            throw new IllegalArgumentException(
                    "Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
        }

        super.setAuthenticated(false);
    }
}
