package com.websocket_hub.provider;

import com.websocket_hub.domain.enums.RoomType;
import org.springframework.http.server.ServerHttpRequest;

import java.util.UUID;

public interface IdentityProvider {

    UUID resolveGuid(ServerHttpRequest request);

    String resolveRoomName(ServerHttpRequest request);

    RoomType resolveRoomType(ServerHttpRequest request);

    String resolveAction(ServerHttpRequest request);

    String resolveToken(ServerHttpRequest request);

    String extractToken(ServerHttpRequest request);
}
