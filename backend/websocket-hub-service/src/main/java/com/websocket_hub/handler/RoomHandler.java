package com.websocket_hub.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.websocket_hub.domain.dto.RoomMessage;
import com.websocket_hub.manager.RoomManager;
import com.websocket_hub.manager.SessionManager;
import com.websocket_hub.util.WebSocketUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;

/**
 * Handler only for testing room broadcast functionality and websocket connections.
 *
 * <p>
 * Method {@link #handleTextMessage} receives messages from clients and broadcasts it to all clients in the same room.
 *
 * <p>
 * Methods {@link #onJoin} and {@link #onLeave} used to handle user join and leave events if needed.
 */
@Component
@Slf4j
public class RoomHandler extends AppWebSocketHandler<RoomManager> {

    private final ObjectMapper objectMapper;

    public RoomHandler(SessionManager sessionManager, RoomManager roomManager, ObjectMapper objectMapper) {
        super(sessionManager, roomManager);
        this.objectMapper = objectMapper;
    }

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.info("Received game message: {}", payload);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = objectMapper.readValue(payload, Map.class);

            String type = (String) data.get("type");
            String event = (String) data.get("event");
            String roomName = WebSocketUtil.getRoomName(session);
            String userId = WebSocketUtil.getUserId(session);
            String content = (String) data.get("content");

            RoomMessage response = RoomMessage.builder()
                    .type(type)
                    .event(event)
                    .fromUserId(userId)
                    .roomName(roomName)
                    .content(content)
                    .build();

            roomManager.broadcast(roomName, response);

        } catch (Exception e) {
            log.error("Failed to handle game message", e);
        }
    }

    @Override
    protected void onJoin(String roomName, String username) {
    }

    @Override
    protected void onLeave(String roomName, String username) {
    }
}
