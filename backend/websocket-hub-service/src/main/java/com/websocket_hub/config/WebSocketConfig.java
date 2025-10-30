package com.websocket_hub.config;

import com.websocket_hub.handler.GameRoomHandler;
import com.websocket_hub.handler.RoomHandler;
import com.websocket_hub.interceptor.UserHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final RoomHandler roomHandler;

    private final GameRoomHandler gameRoomHandler;

    private final UserHandshakeInterceptor handshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(roomHandler, "/ws/room")
                .setAllowedOriginPatterns("*")
                .addInterceptors(handshakeInterceptor);

        registry.addHandler(gameRoomHandler, "/ws/game")
                .setAllowedOriginPatterns("*")
                .addInterceptors(handshakeInterceptor);
    }
}
