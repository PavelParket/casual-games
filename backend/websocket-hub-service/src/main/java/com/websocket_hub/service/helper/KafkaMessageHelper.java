package com.websocket_hub.service.helper;

import com.common_utils.enums.NotificationEventParams;
import com.common_utils.enums.NotificationType;
import com.kafka_starter.config.KafkaTopics;
import com.kafka_starter.dto.event.NotificationEvent;
import com.kafka_starter.dto.event.RoomDeleteEvent;
import com.kafka_starter.service.KafkaMessageService;
import com.websocket_hub.domain.entity.ClientSession;
import com.websocket_hub.domain.entity.Room;
import com.websocket_hub.domain.enums.RoomType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
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

    public void sendRoomInvitedEvent(UUID recipientGuid, ClientSession client, Room room, Duration expiresAt, Instant timestamp) {
        NotificationEvent notificationEvent = NotificationEvent.builder()
                .eventId(UUID.randomUUID())
                .recipientGuid(recipientGuid)
                .type(NotificationType.ROOM_INVITE)
                .params(Map.of(
                        NotificationEventParams.USERNAME.getParam(), client.getUsername(),
                        NotificationEventParams.ROOM_HANDLER.getParam(), room.getType().getHandlerUrl(),
                        NotificationEventParams.ROOM_NAME.getParam(), room.getName(),
                        NotificationEventParams.ROOM_ID.getParam(), room.getId().toString()
                ))
                .expiresAt(timestamp.plus(expiresAt))
                .timestamp(timestamp)
                .build();

        kafkaMessageService.send(kafkaTopics.getUserNotification(), recipientGuid.toString(), notificationEvent);

        log.info("Room invite event sent: recipientGuid={}, roomId={}", recipientGuid, room.getId());
    }
}
