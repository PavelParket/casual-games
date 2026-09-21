package com.casualgames.notifications.domain.dto;

import com.casualgames.commonutils.enums.NotificationType;
import lombok.Builder;

import java.time.Instant;

@Builder
public record NotificationResponse(

        Long id,

        NotificationType type,

        String title,

        String body,

        String link,

        Instant expiresAt,

        Instant readAt,

        Instant createdAt
) {
}
