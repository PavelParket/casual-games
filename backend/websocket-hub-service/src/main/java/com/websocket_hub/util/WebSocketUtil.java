package com.websocket_hub.util;

import com.websocket_hub.domain.dto.user_service.UserInfoInternalResponse;
import lombok.experimental.UtilityClass;
import org.springframework.web.socket.WebSocketSession;

import java.time.Instant;
import java.util.UUID;

@UtilityClass
public class WebSocketUtil {

    public UUID getGuid(WebSocketSession session) {
        return (UUID) session.getAttributes().get("guid");
    }

    public UserInfoInternalResponse getUser(WebSocketSession session) {
        return (UserInfoInternalResponse) session.getAttributes().get("user");
    }

    public String getRoomName(WebSocketSession session) {
        return (String) session.getAttributes().get("roomName");
    }

    public static Instant getConnectedAt(WebSocketSession session) {
        return (Instant) session.getAttributes().get("connectedAt");
    }
}
