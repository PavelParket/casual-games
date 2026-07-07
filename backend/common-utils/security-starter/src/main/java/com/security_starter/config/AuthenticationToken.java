package com.security_starter.config;

import com.security_starter.enums.Role;
import com.security_starter.enums.Status;
import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
public class AuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal;

    private final Object credentials;

    private final UUID guid;

    private final UUID sid;

    private final String email;

    private final Status status;

    private final Set<String> roles;

    private final Set<String> permissions;

    private final Map<String, Set<String>> roleAndPermissionsMap;

    private AuthenticationToken(
            UUID guid,
            UUID sid,
            String email,
            Status status,
            Set<String> permissions,
            Map<String, Set<String>> roleAndPermissionsMap,
            Collection<? extends GrantedAuthority> authorities
    ) {
        super(authorities);
        this.guid = guid;
        this.sid = sid;
        this.email = email;
        this.status = status;
        this.permissions = permissions;
        this.roleAndPermissionsMap = roleAndPermissionsMap;
        this.principal = this;
        this.credentials = null;

        if (authorities != null && !authorities.isEmpty()) {
            this.roles = authorities.stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());
            super.setAuthenticated(true);
        } else {
            this.roles = Set.of();
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
    public String getName() {
        return guid != null ? guid.toString() : "anonymous";
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        if (isAuthenticated) {
            throw new IllegalArgumentException(
                    "Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
        }

        super.setAuthenticated(false);
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
    }

    @Override
    public String toString() {
        return String.format("AuthenticationToken{guid=%s, email=%s, status=%s, roles=%s}", guid, email, status, roles);
    }

    public boolean hasRole(Role role) {
        return roles.contains(role.name());
    }

    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }

    public Set<String> getPermissionsForRole(String role) {
        return roleAndPermissionsMap.getOrDefault(role, Set.of());
    }

    public static AuthenticationToken unauthenticated() {
        return new AuthenticationToken(null, null, null, null, Set.of(), Map.of(), Set.of());
    }

    public static AuthenticationToken authenticated(
            UUID guid,
            UUID sid,
            String email,
            Status status,
            Set<String> roles,
            Set<String> permissions,
            Map<String, Set<String>> roleAndPermissionsMap
    ) {
        Collection<GrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());

        return new AuthenticationToken(guid, sid, email, status, permissions, roleAndPermissionsMap, authorities);
    }
}
