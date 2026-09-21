package com.casualgames.websockethub.config;

import com.casualgames.websockethub.handler.DeCoderGameRoomHandler;
import com.casualgames.websockethub.handler.DurakGameRoomHandler;
import com.casualgames.websockethub.handler.HorseRaceGameRoomHandler;
import com.casualgames.websockethub.handler.MahjongGameRoomHandler;
import com.casualgames.websockethub.handler.TicTacToeGameRoomHandler;
import com.casualgames.websockethub.interceptor.AppHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final AppHandshakeInterceptor handshakeInterceptor;

    private final TicTacToeGameRoomHandler ticTacToeGameRoomHandler;

    private final HorseRaceGameRoomHandler horseRaceGameRoomHandler;

    private final DeCoderGameRoomHandler deCoderGameRoomHandler;

    private final DurakGameRoomHandler durakGameRoomHandler;

    private final MahjongGameRoomHandler mahjongGameRoomHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(ticTacToeGameRoomHandler, "/ws/t-t-t")
                .setAllowedOriginPatterns("*")
                .addInterceptors(handshakeInterceptor);

        registry.addHandler(deCoderGameRoomHandler, "/ws/de-coder")
                .setAllowedOriginPatterns("*")
                .addInterceptors(handshakeInterceptor);

        registry.addHandler(horseRaceGameRoomHandler, "/ws/horse-race")
                .setAllowedOriginPatterns("*")
                .addInterceptors(handshakeInterceptor);

        registry.addHandler(durakGameRoomHandler, "/ws/durak")
                .setAllowedOriginPatterns("*")
                .addInterceptors(handshakeInterceptor);

        registry.addHandler(mahjongGameRoomHandler, "/ws/mahjong")
                .setAllowedOriginPatterns("*")
                .addInterceptors(handshakeInterceptor);
    }
}
