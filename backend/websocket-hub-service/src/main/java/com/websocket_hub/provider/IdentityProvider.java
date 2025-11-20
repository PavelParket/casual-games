package com.websocket_hub.provider;

import org.springframework.http.server.ServerHttpRequest;

import java.util.UUID;

public interface IdentityProvider {

    UUID resolveGuid(ServerHttpRequest request);

    String resolveRoomName(ServerHttpRequest request);

    String resolveToken(ServerHttpRequest request);
}
