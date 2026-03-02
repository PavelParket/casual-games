package com.websocket_hub.client;

import com.websocket_hub.domain.dto.message.DeCoderGameMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeCoderGameServiceClient {

    private final RestTemplate restTemplate;

    @Value("${app.game-service.url}")
    private String gameServiceUrl;

    public DeCoderGameMessage startGame(DeCoderGameMessage request) {
        return sendRequest("/game/de_coder/start", request);
    }

    public DeCoderGameMessage processMove(DeCoderGameMessage request) {
        return sendRequest("/game/de_coder/move", request);
    }

    private DeCoderGameMessage sendRequest(String path, DeCoderGameMessage request) {
        URI uri = UriComponentsBuilder.fromUriString(gameServiceUrl)
                .path(path)
                .build()
                .toUri();

        try {
            ResponseEntity<DeCoderGameMessage> response = restTemplate.exchange(
                    new RequestEntity<>(request, HttpMethod.POST, uri),
                    DeCoderGameMessage.class
            );
            return response.getBody();
        } catch (HttpClientErrorException.TooManyRequests e) {
            assert e.getResponseHeaders() != null;
            throw new RuntimeException("COOLDOWN:" + e.getResponseHeaders().getFirst("Retry-After"));
        } catch (Exception e) {
            log.error("Game service call failed: {}", e.getMessage());
            throw new RuntimeException("Game Error: " + e.getMessage());
        }
    }

    public DeCoderGameMessage getGameState(UUID roomId) {
        URI uri = UriComponentsBuilder.fromUriString(gameServiceUrl)
                .path("/game/de_coder/{roomId}/state")
                .buildAndExpand(roomId)
                .toUri();

        try {
            ResponseEntity<DeCoderGameMessage> response = restTemplate.exchange(
                    new RequestEntity<>(HttpMethod.GET, uri),
                    DeCoderGameMessage.class // <--- Ожидаем JSON-объект
            );

            return response.getBody();
        } catch (Exception e) {
            log.warn("Failed to get game state for room {}: {}", roomId, e.getMessage());
            return null;
        }
    }
}