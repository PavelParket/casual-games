package com.websocket_hub.util;

import com.websocket_hub.domain.dto.client.UserInternalResponse;
import com.websocket_hub.domain.enums.RoomType;
import lombok.experimental.UtilityClass;
import org.springframework.web.socket.WebSocketSession;

import java.time.Instant;
import java.util.UUID;

@UtilityClass
public class WebSocketUtil {

    public UUID getGuid(WebSocketSession session) {
        return (UUID) session.getAttributes().get("guid");
    }

    public UserInternalResponse getUser(WebSocketSession session) {
        return (UserInternalResponse) session.getAttributes().get("user");
    }

    public UUID getRoomId(WebSocketSession session) {
        return (UUID) session.getAttributes().get("roomId");
    }

    public RoomType getRoomType(WebSocketSession session) {
        return (RoomType) session.getAttributes().get("roomType");
    }

    public static Instant getConnectedAt(WebSocketSession session) {
        return (Instant) session.getAttributes().get("connectedAt");
    }

    public String getToken(WebSocketSession session) {
        return (String) session.getAttributes().get("token");
    }
}
