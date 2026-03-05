package com.websocket_hub.interceptor;

import com.websocket_hub.client.UserServiceClient;
import com.websocket_hub.domain.dto.client.UserInternalResponse;
import com.websocket_hub.domain.entity.RoomMetadata;
import com.websocket_hub.domain.enums.RoomStatus;
import com.websocket_hub.domain.enums.redis.RoomTypeRedisKey;
import com.websocket_hub.domain.repository.RoomRedisRepository;
import com.websocket_hub.provider.IdentityProvider;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppHandshakeInterceptor implements HandshakeInterceptor {

    private final IdentityProvider identityProvider;

    private final UserServiceClient client;

    private final RoomRedisRepository roomRedisRepository;

    @Override
    public boolean beforeHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response, @NonNull WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        String ip = request.getRemoteAddress().getHostString();

        try {
            String token = identityProvider.resolveToken(request);
            UUID guid = identityProvider.resolveGuid(request);
            UUID roomId = identityProvider.resolveRoomId(request);
            UserInternalResponse user = client.getUserByGuid(guid, token);

            RoomMetadata metadata = findMetadataByRoomId(roomId);
            if (metadata != null) {
                RoomStatus status = metadata.getStatus() != null ? metadata.getStatus() : RoomStatus.WAITING;

                if (status == RoomStatus.IN_PROGRESS) {
                    log.warn("Handshake rejected — room IN_PROGRESS: user={}, roomId={}", user.email(), roomId);
                    response.setStatusCode(HttpStatus.FORBIDDEN);
                    return false;
                }

                if (status == RoomStatus.FINISHED) {
                    log.warn("Handshake rejected — room FINISHED: user={}, roomId={}", user.email(), roomId);
                    response.setStatusCode(HttpStatus.FORBIDDEN);
                    return false;
                }
            }

            attributes.put("guid", guid);
            attributes.put("user", user);
            attributes.put("roomId", roomId);
            attributes.put("connectedAt", Instant.now());

            log.info("Preparing handshake for user={} room={} ip={}", user.email(), roomId, ip);

            return true;
        } catch (JwtException e) {
            log.warn("Handshake rejected — invalid token: ip={}, reason={}", ip, e.getMessage());
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;

        } catch (IllegalArgumentException e) {
            log.warn("Handshake rejected — bad request params: ip={}, reason={}", ip, e.getMessage());
            response.setStatusCode(HttpStatus.BAD_REQUEST);
            return false;

        } catch (Exception e) {
            log.error("Handshake rejected — internal error: ip={}", ip, e);
            response.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
            return false;
        }
    }

    @Override
    public void afterHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response, @NonNull WebSocketHandler wsHandler, Exception exception) {
        if (exception != null) {
            String ip = request.getRemoteAddress().getHostString();
            log.warn("Handshake failed from ip={}: {}", ip, exception.getMessage());
        }
    }

    private RoomMetadata findMetadataByRoomId(UUID roomId) {
        return Arrays.stream(RoomTypeRedisKey.values())
                .map(key -> roomRedisRepository.get(roomId, key))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }
}
