package com.websocket_hub.service.helper;

import com.kafka_starter.config.KafkaTopics;
import com.kafka_starter.dto.event.RoomDeleteEvent;
import com.kafka_starter.service.KafkaMessageService;
import com.websocket_hub.domain.enums.RoomType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaMessageHelper {

    private final KafkaMessageService kafkaMessageService;

    private final KafkaTopics kafkaTopics;

    public void sendRoomDeletedEvent(UUID roomId, RoomType roomType, String reason) {
        RoomDeleteEvent roomDeleteEvent = RoomDeleteEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .roomId(roomId.toString())
                .roomType(roomType.name())
                .reason(reason)
                .timestamp(Instant.now().toString())
                .build();

        kafkaMessageService.send(kafkaTopics.getRoomLifecycle(), roomId.toString(), roomDeleteEvent);

        log.info("Room deleted event sent: roomId={}, roomType={}, reason={}", roomId, roomType, reason);
    }
}
