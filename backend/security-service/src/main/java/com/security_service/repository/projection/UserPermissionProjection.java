package com.security_service.repository.projection;

public interface UserPermissionProjection {

    String getAttribute();

    String getOperation();

    Boolean getForMe();

    Boolean getForAll();

    Boolean getAllowed();
}
