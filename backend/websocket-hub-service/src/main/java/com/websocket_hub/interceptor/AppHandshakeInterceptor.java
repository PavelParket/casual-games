package com.websocket_hub.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.websocket_hub.client.UserServiceClient;
import com.websocket_hub.domain.dto.ErrorResponse;
import com.websocket_hub.domain.dto.client.UserInternalResponse;
import com.websocket_hub.domain.entity.RoomMetadata;
import com.websocket_hub.domain.enums.RoomStatus;
import com.websocket_hub.domain.enums.redis.RoomTypeRedisKey;
import com.websocket_hub.domain.repository.RoomRedisRepository;
import com.websocket_hub.exception.AuthenticationException;
import com.websocket_hub.exception.BadRequestException;
import com.websocket_hub.exception.ForbiddenException;
import com.websocket_hub.exception.NotFoundException;
import com.websocket_hub.provider.IdentityProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static com.websocket_hub.config.ResourceMessageConstants.ROOM_ALREADY_FINISHED;
import static com.websocket_hub.config.ResourceMessageConstants.ROOM_ALREADY_IN_PROGRESS;
import static com.websocket_hub.config.ResourceMessageConstants.SERVICE_UNAVAILABLE;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppHandshakeInterceptor implements HandshakeInterceptor {

    private final IdentityProvider identityProvider;
    private final UserServiceClient client;
    private final RoomRedisRepository roomRedisRepository;
    private final ObjectMapper objectMapper;

    @Override
    public boolean beforeHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response, @NonNull WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        String ip = request.getRemoteAddress().getHostString();

        try {
            String token = identityProvider.resolveToken(request);
            UUID guid = identityProvider.resolveGuid(request);
            UUID roomId = identityProvider.resolveRoomId(request);
            UserInternalResponse user = client.getUserByGuid(guid, token);

            validateRoomStatus(roomId);

            attributes.put("guid", guid);
            attributes.put("user", user);
            attributes.put("roomId", roomId);
            attributes.put("connectedAt", Instant.now());

            log.info("Handshake OK: user={}, room={}, ip={}", user.email(), roomId, ip);
            return true;

        } catch (AuthenticationException e) {
            log.warn("Handshake rejected — unauthorized: ip={}, reason={}", ip, e.getMessage());
            writeErrorResponse(response, HttpStatus.UNAUTHORIZED, e.getMessage());
            return false;

        } catch (BadRequestException e) {
            log.warn("Handshake rejected — bad request: ip={}, reason={}", ip, e.getMessage());
            writeErrorResponse(response, HttpStatus.BAD_REQUEST, e.getMessage());
            return false;

        } catch (ForbiddenException e) {
            log.warn("Handshake rejected — forbidden: ip={}, reason={}", ip, e.getMessage());
            writeErrorResponse(response, HttpStatus.FORBIDDEN, e.getMessage());
            return false;

        } catch (NotFoundException e) {
            log.warn("Handshake rejected — not found: ip={}, reason={}", ip, e.getMessage());
            writeErrorResponse(response, HttpStatus.NOT_FOUND, e.getMessage());
            return false;

        } catch (Exception e) {
            log.error("Handshake rejected — internal error: ip={}", ip, e);
            writeErrorResponse(response, HttpStatus.SERVICE_UNAVAILABLE, SERVICE_UNAVAILABLE);
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

    private void validateRoomStatus(UUID roomId) {
        RoomMetadata metadata = findMetadataByRoomId(roomId);

        if (metadata == null) {
            return;
        }

        RoomStatus status = metadata.getStatus();

        if (RoomStatus.FINISHED.equals(status)) {
            throw new ForbiddenException(ROOM_ALREADY_FINISHED);
        } else if (RoomStatus.IN_PROGRESS.equals(status) && !metadata.getType().isAllowsLateJoin()) {
            throw new ForbiddenException(ROOM_ALREADY_IN_PROGRESS);
        }
    }

    private void writeErrorResponse(ServerHttpResponse response, HttpStatus status, String message) {
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(status.value())
                .message(message)
                .timestamp(Instant.now())
                .build();
        
        try {
            byte[] body = objectMapper.writeValueAsBytes(errorResponse);
            response.getBody().write(body);
            response.getBody().flush();
        } catch (IOException e) {
            log.error("Failed to write error response", e);
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
