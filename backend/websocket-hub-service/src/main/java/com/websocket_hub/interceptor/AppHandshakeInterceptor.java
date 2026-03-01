package com.websocket_hub.interceptor;

import com.websocket_hub.client.UserServiceClient;
import com.websocket_hub.domain.dto.client.UserInternalResponse;
import com.websocket_hub.domain.enums.RoomType;
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
public class AppHandshakeInterceptor implements HandshakeInterceptor {

    private final IdentityProvider identityProvider;

    private final UserServiceClient client;

    @Override
    public boolean beforeHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response, @NonNull WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        String token = identityProvider.resolveToken(request);
        UUID guid = identityProvider.resolveGuid(request);
        UUID roomId = identityProvider.resolveRoomId(request);
        RoomType roomType = identityProvider.resolveRoomType(request);
        UserInternalResponse user = client.getUserByGuid(guid, token);
        String ip = request.getRemoteAddress().getHostString();

        attributes.put("guid", guid);
        attributes.put("user", user);
        attributes.put("roomId", roomId);
        attributes.put("roomType", roomType);
        attributes.put("connectedAt", Instant.now());

        log.info("Preparing handshake for user={} room={} type={} ip={}", user.email(), roomId, roomType, ip);

        return true;
    }

    @Override
    public void afterHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response, @NonNull WebSocketHandler wsHandler, Exception exception) {
        if (exception != null) {
            String ip = request.getRemoteAddress().getHostString();
            log.warn("Handshake failed from ip={}: {}", ip, exception.getMessage());
        }
    }
}
