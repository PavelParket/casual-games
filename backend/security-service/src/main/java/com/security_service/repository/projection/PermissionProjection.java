package com.security_service.repository.projection;

public interface PermissionProjection {

    String getAttribute();

    String getOperation();

    Boolean getForMe();

    Boolean getForAll();
}
