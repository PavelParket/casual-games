package com.websocket_hub.provider;

import com.websocket_hub.jwt.JwtProvider;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtIdentityProvider implements IdentityProvider {

    private final JwtProvider provider;

    @Value("${app.websocket.default-room}")
    private String defaultRoom;

    @Override
    public String resolveUserId(ServerHttpRequest request) {
        var params = UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams();

        String token = params.getFirst("token");

        if (token == null || token.isBlank()) {
            String guestId = "guest-" + System.currentTimeMillis();

            log.debug("Anonymous connection — generated guest id {}", guestId);

            return guestId;
        }

        if (!provider.validate(token)) {
            throw new JwtException("Invalid JWT token!");
        }

        String userId = provider.getEmail(token);

        return (userId != null && !userId.isBlank()) ? userId : "guest-" + System.currentTimeMillis();
    }

    // TODO: send the request to user service, receive data and resolve need
    @Override
    public String resolveUsername(ServerHttpRequest request) {
        return "Pavel";
    }

    @Override
    public String resolveRoomName(ServerHttpRequest request) {
        var params = UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams();

        String roomId = params.getFirst("roomId");

        return (roomId == null || roomId.isBlank()) ? defaultRoom : roomId;
    }
}
