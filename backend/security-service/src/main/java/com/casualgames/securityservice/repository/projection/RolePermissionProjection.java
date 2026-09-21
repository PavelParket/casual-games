package com.casualgames.securityservice.repository.projection;

public interface RolePermissionProjection {

    String getRoleName();

    String getAttribute();

    String getOperation();

    Boolean getForMe();

    Boolean getForAll();
}
