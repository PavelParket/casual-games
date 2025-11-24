package com.websocket_hub.provider;

import com.websocket_hub.domain.enums.RoomType;
import com.websocket_hub.jwt.JwtProvider;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtIdentityProvider implements IdentityProvider {

    private final JwtProvider provider;

    @Value("${app.websocket.default-room}")
    private String defaultRoom;

    @Override
    public UUID resolveGuid(ServerHttpRequest request) {
        var params = UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams();

        String token = params.getFirst("token");

        if (!provider.isToken(token)) {
            throw new JwtException("Missing JWT token!");
        }

        if (!provider.validate(token)) {
            throw new JwtException("Invalid JWT token!");
        }

        String guid = provider.getGuid(token);

        if (guid == null || guid.isBlank()) {
            throw new JwtException("JWT token does not contain GUID!");
        }

        try {
            return UUID.fromString(guid);
        } catch (IllegalArgumentException e) {
            throw new JwtException("Malformed GUID inside JWT: " + guid, e);
        }
    }

    @Override
    public String resolveRoomName(ServerHttpRequest request) {
        var params = UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams();

        String roomName = params.getFirst("roomName");

        if (roomName == null || roomName.isBlank()) {
            throw new IllegalArgumentException("Missing room name parameter!");
        }

        return roomName;
    }

    @Override
    public RoomType resolveRoomType(ServerHttpRequest request) {
        var params = UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams();

        String roomType = params.getFirst("roomType");

        if (roomType == null || roomType.isBlank()) {
            throw new IllegalArgumentException("Missing room type parameter!");
        }

        return RoomType.valueOf(roomType);
    }

    @Override
    public String resolveToken(ServerHttpRequest request) {
        var params = UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams();

        String token = params.getFirst("token");

        if (!provider.isToken(token)) {
            throw new JwtException("Missing JWT token!");
        }

        if (!provider.validate(token)) {
            throw new JwtException("Invalid JWT token!");
        }

        return token;
    }

    public String extractToken(ServerHttpRequest request) {
        return request.getHeaders().getFirst("Authorization");
    }
}
