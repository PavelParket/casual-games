package com.kafka_starter.dto.event;

import com.common_utils.enums.NotificationType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationEvent {

    private UUID eventId;

    private UUID recipientGuid;

    private NotificationType type;

    @Builder.Default
    private Map<String, String> params = new HashMap<>();

    private Instant expiresAt;

    private Instant timestamp;
}
