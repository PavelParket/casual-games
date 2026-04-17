package com.security_service.service.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafka_starter.dto.event.sync.SynchronizedUser;
import com.security_service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SynchronizedUserListener {

    private final ObjectMapper objectMapper;

    private final UserService userService;

    @KafkaListener(topics = "#{kafkaTopics.user}", groupId = "${kafka.consumer-config.[group.id]}")
    public void handleUserChange(String message) {
        try {
            SynchronizedUser synchronizedUser = objectMapper.readValue(message, SynchronizedUser.class);

            log.info("Processing user sync event: guid={}", synchronizedUser.getGuid());

            userService.synchronizeUpdatedUser(synchronizedUser);
        } catch (Exception e) {
            log.error("Failed to process user sync event: message={}", message, e);
            throw new RuntimeException("User sync event processing failed", e);
        }
    }
}
