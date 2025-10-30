package com.websocket_hub.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameServiceClient {

    private final RestTemplate restTemplate;

    @Value("${app.game-service.url}")
    private String gameServiceUrl;

    public Map<String, Object> startGame(Map<String, Object> request) {
        String url = gameServiceUrl + "/game/t-t-t/start";

        log.debug("Calling game-service to start game: {}", request);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);

            log.debug("Game started successfully: {}", response);

            return response;
        } catch (Exception e) {
            log.error("Failed to call game-service start endpoint", e);
            throw new RuntimeException("Failed to start game", e);
        }
    }

    public Map<String, Object> processMove(Map<String, Object> request) {
        String url = gameServiceUrl + "/game/t-t-t/move";

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
