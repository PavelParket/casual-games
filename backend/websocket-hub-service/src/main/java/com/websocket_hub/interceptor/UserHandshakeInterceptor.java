package com.websocket_hub.interceptor;

import com.websocket_hub.client.UserServiceClient;
import com.websocket_hub.domain.dto.user_service.UserInfoInternalResponse;
import com.websocket_hub.domain.entity.Room;
import com.websocket_hub.domain.enums.RoomType;
import com.websocket_hub.provider.IdentityProvider;
import com.websocket_hub.service.RoomService;
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

    private final RoomService service;

    @Override
    public boolean beforeHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response, @NonNull WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        UUID guid = identityProvider.resolveGuid(request);
        String roomName = identityProvider.resolveRoomName(request);
        RoomType roomType = identityProvider.resolveRoomType(request);
        String action = identityProvider.resolveAction(request);
        UserInfoInternalResponse user = client.getUserByGuid(guid);
        String ip = request.getRemoteAddress().getHostString();

        Map<String, Room> rooms = service.getRoomsByType(roomType);

        if ("join".equals(action) && !rooms.containsKey(roomName)) {
            log.warn("Join to non-existent room {}", roomName);
            throw new IllegalArgumentException("Room does not exist");
        }

        attributes.put("guid", guid);
        attributes.put("user", user);
        attributes.put("roomName", roomName);
        attributes.put("roomType", roomType);
        attributes.put("connectedAt", Instant.now());
        attributes.put("action", action);

        log.info("Preparing handshake for user={} room={} type={} action={} ip={}", user.email(), roomName, roomType, action, ip);

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
