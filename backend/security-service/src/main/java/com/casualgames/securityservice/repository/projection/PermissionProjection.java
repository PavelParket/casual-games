package com.casualgames.securityservice.repository.projection;

public interface PermissionProjection {

    String getAttribute();

    String getOperation();

    Boolean getForMe();

    Boolean getForAll();
}
