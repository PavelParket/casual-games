package com.casualgames.securitystarter.provider;

import com.casualgames.securitystarter.config.AuthenticationToken;

import java.util.Optional;
import java.util.Set;

public interface PermissionProvider {

    Set<String> loadPermissions(Set<String> roles, String email);

    Optional<AuthenticationToken> getToken();
}
