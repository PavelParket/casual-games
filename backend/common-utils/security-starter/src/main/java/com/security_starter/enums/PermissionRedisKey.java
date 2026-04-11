package com.security_starter.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PermissionRedisKey {

    ROLE("permissions:role"),
    USER_ALLOW("permissions:user:allow"),
    USER_RESTRICT("permissions:user:restrict");

    private final String key;
}
