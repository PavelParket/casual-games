package com.game_service.common.service.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.game_service.common.service.GameService;
import com.kafka_starter.dto.event.RoomDeleteEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoomLifecycleEventListener {

    private final ObjectMapper objectMapper;

    private final GameService gameService;

    @KafkaListener(
            topics = "#{kafkaTopics.roomLifecycle}",
            groupId = "${kafka.consumer-config.[group.id]}"
    )
    public void onRoomDeleted(String message) {
        try {
            RoomDeleteEvent event = objectMapper.readValue(message, RoomDeleteEvent.class);

            log.info("Received room deleted event: roomId={}, roomType={}, reason={}", event.getRoomId(), event.getRoomType(), event.getReason());

            gameService.handleRoomDeleted(event);
        } catch (Exception e) {
            log.error("Failed to process room deleted event, message={}", message, e);
        }
    }
}
