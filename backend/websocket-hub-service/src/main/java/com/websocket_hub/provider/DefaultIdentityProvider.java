package com.websocket_hub.provider;

import com.websocket_hub.exception.AuthenticationException;
import com.websocket_hub.exception.NotFoundException;
import com.websocket_hub.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

import static com.websocket_hub.config.ResourceMessageConstants.AUTHENTICATION_FAILED;
import static com.websocket_hub.config.ResourceMessageConstants.ROOM_NOT_FOUND;

@Component
@RequiredArgsConstructor
@Slf4j
public class DefaultIdentityProvider implements IdentityProvider {

    private final JwtProvider provider;

    @Override
    public UUID resolveGuid(ServerHttpRequest request) {
        String token = resolveToken(request);

        String guid = provider.getGuid(token);

        if (guid == null || guid.isBlank()) {
            throw new AuthenticationException(AUTHENTICATION_FAILED);
        }

        try {
            return UUID.fromString(guid);
        } catch (IllegalArgumentException e) {
            throw new AuthenticationException(AUTHENTICATION_FAILED);
        }
    }

    @Override
    public UUID resolveRoomId(ServerHttpRequest request) {
        var params = UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams();

        String roomId = params.getFirst("roomId");

        if (roomId == null || roomId.isBlank()) {
            throw new NotFoundException(ROOM_NOT_FOUND);
        }

        try {
            return UUID.fromString(roomId);
        } catch (IllegalArgumentException e) {
            throw new NotFoundException(ROOM_NOT_FOUND);
        }
    }

    @Override
    public String resolveToken(ServerHttpRequest request) {
        var params = UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams();

        String token = params.getFirst("token");

        if (!provider.isToken(token)) {
            throw new AuthenticationException(AUTHENTICATION_FAILED);
        }

        if (!provider.validate(token)) {
            throw new AuthenticationException(AUTHENTICATION_FAILED);
        }

        return token;
    }

    @Override
    public String extractToken(ServerHttpRequest request) {
        return request.getHeaders().getFirst("Authorization");
    }
}
