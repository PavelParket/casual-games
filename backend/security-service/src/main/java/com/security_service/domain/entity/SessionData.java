package com.security_service.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@AllArgsConstructor
@Builder
public class SessionData {

    String refreshHash;

    String deviceLabel;

    long createdAt;

    long lastUsedAt;
}
