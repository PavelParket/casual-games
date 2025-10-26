package com.websocket_hub.util;

import lombok.experimental.UtilityClass;
import org.springframework.web.socket.WebSocketSession;

@UtilityClass
public class WebSocketUtil {

    public String getUserId(WebSocketSession session) {
        return (String) session.getAttributes().get("userId");
    }

    public String getUsername(WebSocketSession session) {
        return (String) session.getAttributes().get("username");
    }

    public String getRoomName(WebSocketSession session) {
        return (String) session.getAttributes().get("roomName");
    }
}
