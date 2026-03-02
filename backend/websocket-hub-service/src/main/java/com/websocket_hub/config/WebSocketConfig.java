package com.websocket_hub.config;

import com.websocket_hub.handler.DeCoderGameRoomHandler;
import com.websocket_hub.handler.HorseRaceGameRoomHandler;
import com.websocket_hub.handler.RoomHandler;
import com.websocket_hub.handler.TicTacToeGameRoomHandler;
import com.websocket_hub.interceptor.AppHandshakeInterceptor;
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

    private final TicTacToeGameRoomHandler ticTacToeGameRoomHandler;

    private final HorseRaceGameRoomHandler horseRaceGameRoomHandler;

    private final DeCoderGameRoomHandler deCoderGameRoomHandler;

    private final AppHandshakeInterceptor handshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(roomHandler, "/ws/room")
                .setAllowedOriginPatterns("*")
                .addInterceptors(handshakeInterceptor);

        registry.addHandler(ticTacToeGameRoomHandler, "/ws/t-t-t")
                .setAllowedOriginPatterns("*")
                .addInterceptors(handshakeInterceptor);

        registry.addHandler(deCoderGameRoomHandler, "/ws/de-coder")
                .setAllowedOriginPatterns("*")
                .addInterceptors(handshakeInterceptor);

        registry.addHandler(horseRaceGameRoomHandler, "/ws/horse-race")
                .setAllowedOriginPatterns("*")
                .addInterceptors(handshakeInterceptor);
    }
}
