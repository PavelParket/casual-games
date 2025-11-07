package com.websocket_hub.client;

import com.websocket_hub.domain.dto.GameMessage;
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
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameServiceClient {

    private final RestTemplate restTemplate;

    @Value("${app.game-service.url}")
    private String gameServiceUrl;

    public Optional<GameMessage> startGame(GameMessage request) {
        URI uri = UriComponentsBuilder.fromUriString(gameServiceUrl + "/game/t-t-t/start")
                .path("/game/t-t-t/start")
                .build()
                .toUri();

        log.debug("Calling game-service to start game: {}", request);

        try {
            ResponseEntity<GameMessage> response = restTemplate.exchange(
                    new RequestEntity<>(
                            request,
                            HttpMethod.POST,
                            uri
                    ),
                    GameMessage.class
            );

            log.debug("Game started successfully: {}", response);

            return Optional.ofNullable(response.getBody());
        } catch (Exception e) {
            log.error("Failed to start game", e);
            throw new RuntimeException("Failed to start game", e);
        }
    }

    // TODO: Refactor
    public Map<String, Object> processMove(Map<String, Object> request) {
        URI uri = UriComponentsBuilder.fromUriString(gameServiceUrl + "/game/t-t-t/move")
                .path("game/t-t-t/move")
                .build()
                .toUri();

        log.debug("Calling game-service to process move: {}", request);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);

            log.debug("Move processed successfully: {}", response);

            return response;
        } catch (Exception e) {
            log.error("Failed to call game-service move endpoint", e);
            throw new RuntimeException("Failed to process move", e);
        }
    }
}
