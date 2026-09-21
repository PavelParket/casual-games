package com.casualgames.websockethub.service.listener;

import com.casualgames.kafkastarter.dto.event.sync.SynchronizedFriendship;
import com.casualgames.websockethub.service.FriendshipService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SynchronizedFriendshipListener {

    private final ObjectMapper objectMapper;

    private final FriendshipService friendshipService;

    @KafkaListener(topics = "#{kafkaTopics.friendship}", groupId = "${kafka.consumer-config.[group.id]}")
    public void handleFriendshipChange(String message) {
        try {
            SynchronizedFriendship event = objectMapper.readValue(message, SynchronizedFriendship.class);

            log.info("Processing friendship sync event: type={}, userGuid={}, friendGuid={}", event.getType(), event.getUserGuid(), event.getFriendGuid());

            friendshipService.synchronizeUpdatedFriendship(event);
        } catch (Exception e) {
            log.error("Failed to process friendship sync event: message={}", message, e);
            throw new RuntimeException("Friendship sync event processing failed", e);
        }
    }
}
