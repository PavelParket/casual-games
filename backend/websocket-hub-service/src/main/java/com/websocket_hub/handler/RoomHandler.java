package com.websocket_hub.handler;

import com.websocket_hub.domain.dto.client.UserInternalResponse;
import com.websocket_hub.domain.dto.message.DefaultMessage;
import com.websocket_hub.manager.RoomManager;
import com.websocket_hub.manager.SessionManager;
import com.websocket_hub.serializer.MessageDeserializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.UUID;

/**
 * <h2>
 * RoomHandler
 *
 * <h4>
 * Handler only for testing room broadcast functionality and websocket connections.
 * Don't use it in production!
 *
 * <p>
 * Method {@link #handleTextMessage} receives messages from clients and broadcasts it to all clients in the same room.
 *
 * <p>
 * Methods {@link AppWebSocketHandler#onJoin} and {@link AppWebSocketHandler#onLeave} used to handle user join and
 * leave events if needed.
 */

@Deprecated
@Component
@Slf4j
public class RoomHandler extends AppWebSocketHandler<RoomManager> {

    private final MessageDeserializer deserializer;

    public RoomHandler(SessionManager sessionManager, RoomManager roomManager, MessageDeserializer messageDeserializer) {
        super(sessionManager, roomManager);
        this.deserializer = messageDeserializer;
    }

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();

        if (payload.isBlank()) {
            log.warn("Received empty message from session {}", session.getId());

            return;
        }

        try {
            DefaultMessage defaultMessage = deserializer.deserialize(payload, DefaultMessage.class);

            log.info("Received game message: {}", defaultMessage);

            roomManager.broadcast(defaultMessage.roomId(), defaultMessage);

        } catch (Exception e) {
            log.error("Failed to handle game message", e);
        }
    }

    @Override
    protected void onJoin(UUID roomId, UserInternalResponse user) {
    }

    @Override
    protected void onLeave(UUID roomId, UserInternalResponse user) {
    }
}
