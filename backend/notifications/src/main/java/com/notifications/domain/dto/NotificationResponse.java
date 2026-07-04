package com.notifications.domain.dto;

import com.notifications.domain.enums.NotificationType;
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
