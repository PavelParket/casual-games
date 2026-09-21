package com.casualgames.notifications.domain.dto;

import lombok.Builder;

import java.time.Instant;

@Builder
public record AnnouncementResponse(

        Long id,

        String title,

        String body,

        Instant createdAt
) {
}
