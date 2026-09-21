package com.casualgames.notifications.service.listener;

import com.casualgames.kafkastarter.dto.event.NotificationEvent;
import com.casualgames.notifications.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final ObjectMapper objectMapper;

    private final NotificationService notificationService;

    @KafkaListener(topics = "#{kafkaTopics.userNotification}", groupId = "${kafka.consumer-config.[group.id]}")
    public void handleNotificationEvent(String message) {
        try {
            NotificationEvent event = objectMapper.readValue(message, NotificationEvent.class);

            notificationService.handleNotificationEvent(event);
        } catch (Exception e) {
            log.error("Failed to process notification event: message={}", message, e);
            throw new RuntimeException("Notification event processing failed", e);
        }
    }
}
