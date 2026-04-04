package com.security_service.repository.projection;

public interface RolePermissionProjection {

    String getRoleName();

    String getAttribute();

    String getOperation();

    Boolean getForMe();

    Boolean getForAll();
}
