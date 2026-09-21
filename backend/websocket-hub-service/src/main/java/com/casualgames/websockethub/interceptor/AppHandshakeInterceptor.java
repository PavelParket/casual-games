package com.casualgames.websockethub.interceptor;

import com.casualgames.commonutils.exception.BadRequestException;
import com.casualgames.commonutils.exception.ForbiddenException;
import com.casualgames.commonutils.exception.JwtException;
import com.casualgames.commonutils.exception.NotFoundException;
import com.casualgames.websockethub.domain.dto.ErrorResponse;
import com.casualgames.websockethub.domain.dto.client.UserInternalResponse;
import com.casualgames.websockethub.domain.entity.RoomMetadata;
import com.casualgames.websockethub.domain.entity.WsTicketData;
import com.casualgames.websockethub.domain.enums.RoomStatus;
import com.casualgames.websockethub.domain.enums.redis.RoomTypeRedisKey;
import com.casualgames.websockethub.domain.repository.RoomRedisRepository;
import com.casualgames.websockethub.provider.IdentityProvider;
import com.casualgames.websockethub.service.grpc.client.GrpcUserClient;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static com.casualgames.websockethub.config.ResourceMessageConstants.ROOM_ALREADY_FINISHED;
import static com.casualgames.websockethub.config.ResourceMessageConstants.ROOM_ALREADY_IN_PROGRESS;
import static com.casualgames.websockethub.config.ResourceMessageConstants.ROOM_IS_FULL;
import static com.casualgames.websockethub.config.ResourceMessageConstants.ROOM_NOT_FOUND;
import static com.casualgames.websockethub.config.ResourceMessageConstants.SERVICE_UNAVAILABLE;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppHandshakeInterceptor implements HandshakeInterceptor {

    private final IdentityProvider identityProvider;
    private final GrpcUserClient grpcUserClient;
    private final RoomRedisRepository roomRedisRepository;
    private final ObjectMapper objectMapper;

    @Override
    public boolean beforeHandshake(@NonNull ServerHttpRequest request,
                                   @NonNull ServerHttpResponse response,
                                   @NonNull WebSocketHandler wsHandler,
                                   @NonNull Map<String, Object> attributes) throws Exception {
        String ip = request.getRemoteAddress().getHostString();

        try {
            WsTicketData ticket = identityProvider.resolveTicket(request);
            UserInternalResponse user = grpcUserClient.getByGuid(ticket.getUserGuid());

            validateRoom(ticket.getRoomId());

            attributes.put("guid", ticket.getUserGuid());
            attributes.put("user", user);
            attributes.put("roomId", ticket.getRoomId());
            attributes.put("tokenSid", ticket.getTokenSid());
            attributes.put("connectedAt", Instant.now());

            log.debug("Handshake OK: user={}, room={}", user.email(), ticket.getRoomId());

            return true;

        } catch (JwtException e) {
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
    public void afterHandshake(@NonNull ServerHttpRequest request,
                               @NonNull ServerHttpResponse response,
                               @NonNull WebSocketHandler wsHandler,
                               Exception exception) {
        if (exception != null) {
            String ip = request.getRemoteAddress().getHostString();
            log.warn("Handshake failed from ip={}: {}", ip, exception.getMessage());
        }
    }

    private void validateRoom(UUID roomId) {
        RoomMetadata metadata = findMetadataByRoomId(roomId);

        if (metadata == null) {
            throw new NotFoundException(ROOM_NOT_FOUND);
        }

        RoomStatus status = metadata.getStatus();

        if (RoomStatus.FINISHED.equals(status)) {
            throw new ForbiddenException(ROOM_ALREADY_FINISHED);
        } else if (RoomStatus.IN_PROGRESS.equals(status) && !metadata.getType().isAllowsLateJoin()) {
            throw new ForbiddenException(ROOM_ALREADY_IN_PROGRESS);
        }

        if (metadata.getParticipantCount() >= metadata.getType().getMaxParticipants()) {
            throw new ForbiddenException(ROOM_IS_FULL);
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
