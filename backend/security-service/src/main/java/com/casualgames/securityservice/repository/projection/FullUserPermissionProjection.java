package com.casualgames.securityservice.repository.projection;

public interface FullUserPermissionProjection {

    String getEmail();

    String getAttribute();

    String getOperation();

    Boolean getForMe();

    Boolean getForAll();

    Boolean getAllowed();
}
