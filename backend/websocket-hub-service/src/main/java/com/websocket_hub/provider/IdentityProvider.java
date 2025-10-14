package com.websocket_hub.provider;

import org.springframework.http.server.ServerHttpRequest;

public interface IdentityProvider {

    String resolveUserId(ServerHttpRequest request);

    String resolveUsername(ServerHttpRequest request);

    String resolveRoomId(ServerHttpRequest request);
}
