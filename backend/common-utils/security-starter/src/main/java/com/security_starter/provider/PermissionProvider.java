package com.security_starter.provider;

import com.security_starter.config.AuthenticationToken;

import java.util.Optional;
import java.util.Set;

public interface PermissionProvider {

    Set<String> loadPermissions(Set<String> roles, String email);

    Optional<AuthenticationToken> getToken();
}
