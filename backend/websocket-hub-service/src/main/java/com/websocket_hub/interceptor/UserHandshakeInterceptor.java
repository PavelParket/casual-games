package com.websocket_hub.interceptor;

import com.websocket_hub.provider.IdentityProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.time.Instant;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserHandshakeInterceptor implements HandshakeInterceptor {

    private final IdentityProvider identityProvider;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        String userId = identityProvider.resolveUserId(request);
        String username = identityProvider.resolveUsername(request);
        String roomName = identityProvider.resolveRoomName(request);

        if (isBlank(userId) || isBlank(username) || isBlank(roomName)) {
            log.warn("Handshake rejected: invalid params userId={}, username={}, roomName={}", userId, username, roomName);
            response.setStatusCode(HttpStatus.BAD_REQUEST);
            return false;
        }

        attributes.put("userId", userId);
        attributes.put("username", username);
        attributes.put("roomName", roomName);
        attributes.put("connectedAt", Instant.now());

        log.debug("Preparing handshake for user={} room={} ip={}", userId, roomName, request.getRemoteAddress().getHostString());

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        String ip = request.getRemoteAddress().getHostString();

        if (exception == null) {
            try {
                String userId = identityProvider.resolveUserId(request);
                String username = identityProvider.resolveUsername(request);
                String roomName = identityProvider.resolveRoomName(request);

                log.info("Handshake complete: user={} ({}) joined room='{}' from ip={}", username, userId, roomName, ip);
            } catch (Exception e) {
                log.warn("Handshake post-processing failed: {}", e.getMessage());
            }
        } else {
            log.warn("Handshake failed from ip={}: {}", ip, exception.getMessage());
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
