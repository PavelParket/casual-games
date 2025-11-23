package com.websocket_hub.client;

import com.websocket_hub.domain.dto.TicTacToeGameMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameServiceClient {

    private final RestTemplate restTemplate;

    @Value("${app.game-service.url}")
    private String gameServiceUrl;

    public Optional<TicTacToeGameMessage> startGame(TicTacToeGameMessage request) {
        URI uri = UriComponentsBuilder.fromUriString(gameServiceUrl)
                .path("/game/t-t-t/start")
                .build()
                .toUri();

        log.info("Calling game-service to start game: {}", request);

        try {
            ResponseEntity<TicTacToeGameMessage> response = restTemplate.exchange(
                    new RequestEntity<>(
                            request,
                            HttpMethod.POST,
                            uri
                    ),
                    TicTacToeGameMessage.class
            );

            log.info("Game started successfully: {}", response);

            return Optional.ofNullable(response.getBody());
        } catch (Exception e) {
            log.error("Failed to start game {}", e.getMessage(), e);
            throw new RuntimeException("Failed to start game" + e.getMessage(), e);
        }
    }

    public Optional<TicTacToeGameMessage> processMove(TicTacToeGameMessage request) {
        URI uri = UriComponentsBuilder.fromUriString(gameServiceUrl)
                .path("game/t-t-t/move")
                .build()
                .toUri();

        log.info("Calling game-service to process move: {}", request);

        try {
            ResponseEntity<TicTacToeGameMessage> response = restTemplate.exchange(
                    new RequestEntity<>(
                            request,
                            HttpMethod.POST,
                            uri
                    ), TicTacToeGameMessage.class
            );

            log.info("Move processed successfully: {}", response);

            return Optional.ofNullable(response.getBody());
        } catch (Exception e) {
            log.error("Failed to process move {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process move" + e.getMessage(), e);
        }
    }
}
