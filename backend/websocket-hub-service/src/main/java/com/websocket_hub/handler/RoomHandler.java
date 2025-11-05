package com.websocket_hub.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.websocket_hub.domain.dto.RoomResponse;
import com.websocket_hub.manager.RoomManager;
import com.websocket_hub.manager.SessionManager;
import com.websocket_hub.util.WebSocketUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;

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
        log.debug("Received game message: {}", payload);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = objectMapper.readValue(payload, Map.class);

            String type = (String) data.get("type");
            String roomId = WebSocketUtil.getRoomName(session);
            String userId = WebSocketUtil.getUserId(session);
            String content = (String) data.get("content");

            RoomResponse response = RoomResponse.builder()
                    .type(type)
                    .fromUserId(userId)
                    .roomId(roomId)
                    .content(content)
                    .build();

            roomManager.broadcast(roomId, response);

        } catch (Exception e) {
            log.error("Failed to handle game message", e);
        }
    }

    @Override
    protected void onJoin(String roomId, String username) {
    }

    @Override
    protected void onLeave(String roomId, String username) {
    }
}
