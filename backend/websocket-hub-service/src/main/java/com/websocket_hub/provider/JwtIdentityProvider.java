package com.websocket_hub.provider;

import com.websocket_hub.jwt.JwtProvider;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class JwtIdentityProvider implements IdentityProvider {

    private final JwtProvider provider;

    @Override
    public String resolveUserId(ServerHttpRequest request) {
        var params = UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams();

        String token = params.getFirst("token");

        if (token == null || token.isBlank()) {
            return "guest-" + System.currentTimeMillis();
        }

        if (!provider.validate(token)) {
            throw new JwtException("Invalid JWT token!");
        }

        //String userId = Long.toString(provider.getUserId(token));
        String userId = provider.getUsername(token);

        return !userId.isBlank() ? userId : "guest-" + System.currentTimeMillis();
    }

    @Override
    public String resolveRoomId(ServerHttpRequest request) {
        var params = UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams();

        String roomId = params.getFirst("roomId");

        return (roomId == null || roomId.isBlank()) ? "default" : roomId;
    }
}
