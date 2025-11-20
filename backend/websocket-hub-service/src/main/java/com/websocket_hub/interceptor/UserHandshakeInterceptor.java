package com.websocket_hub.interceptor;

import com.websocket_hub.client.UserServiceClient;
import com.websocket_hub.domain.dto.user_service.UserInfoInternalResponse;
import com.websocket_hub.provider.IdentityProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserHandshakeInterceptor implements HandshakeInterceptor {

    private final IdentityProvider identityProvider;

    private final UserServiceClient client;

    @Override
    public boolean beforeHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response, @NonNull WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        UUID guid = identityProvider.resolveGuid(request);
        String roomName = identityProvider.resolveRoomName(request);
        UserInfoInternalResponse user = client.getUserByGuid(guid);
        String ip = request.getRemoteAddress().getHostString();

        attributes.put("user", user);
        attributes.put("guid", guid);
        attributes.put("roomName", roomName);
        attributes.put("connectedAt", Instant.now());

        log.info("Preparing handshake for user={} room={} ip={}", user.email(), roomName, ip);

        return true;
    }

    @Override
    public void afterHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response, @NonNull WebSocketHandler wsHandler, Exception exception) {
        String ip = request.getRemoteAddress().getHostString();

        if (exception == null) {
            try {
                UserInfoInternalResponse user = (UserInfoInternalResponse) request.getAttributes().get("user");
                UUID guid = (UUID) request.getAttributes().get("guid");
                String roomName = (String) request.getAttributes().get("roomName");

                log.info("Handshake complete: user={} ({}) joined room='{}' from ip={}", user.email(), guid, roomName, ip);
            } catch (Exception e) {
                log.warn("Handshake post-processing failed: {}", e.getMessage());
            }
        } else {
            log.warn("Handshake failed from ip={}: {}", ip, exception.getMessage());
        }
    }
}
