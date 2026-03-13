package com.websocket_hub.provider;

import org.springframework.http.server.ServerHttpRequest;

import java.util.UUID;

public interface IdentityProvider {

    UUID resolveGuid(ServerHttpRequest request);

    UUID resolveRoomId(ServerHttpRequest request);

    String resolveToken(ServerHttpRequest request);

    String extractToken(ServerHttpRequest request);
}
