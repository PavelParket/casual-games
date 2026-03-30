package com.websocket_hub.client;

import com.websocket_hub.domain.dto.client.DurakGameInternalRequest;
import com.websocket_hub.domain.dto.client.DurakGameInternalResponse;
import com.websocket_hub.domain.dto.client.DeCoderGameInternalRequest;
import com.websocket_hub.domain.dto.client.DeCoderGameInternalResponse;
import com.websocket_hub.domain.dto.client.HorseRaceGameInternalRequest;
import com.websocket_hub.domain.dto.client.HorseRaceGameInternalResponse;
import com.websocket_hub.domain.dto.message.TicTacToeGameMessage;
import com.websocket_hub.domain.enums.ErrorCode;
import com.websocket_hub.exception.GameException;
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
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameServiceClient {

    @Value("${app.game-service.url}")
    private String gameServiceUrl;

    private final RestTemplate restTemplate;

    // -------------------------------------------------------------------------
    // TicTacToe
    // -------------------------------------------------------------------------

    public TicTacToeGameMessage startGame(TicTacToeGameMessage request) {
        URI uri = UriComponentsBuilder.fromUriString(gameServiceUrl)
                .path("/game/t-t-t/start")
                .build()
                .toUri();

        log.info("Calling game-service to start tic-tac-toe game: {}", request);

        try {
            ResponseEntity<TicTacToeGameMessage> response = restTemplate.exchange(
                    new RequestEntity<>(request, HttpMethod.POST, uri),
                    TicTacToeGameMessage.class
            );

            TicTacToeGameMessage body = response.getBody();

            if (body == null) {
                throw new GameException(ErrorCode.GAME_NOT_STARTED);
            }

            log.info("Game started successfully: roomId={}", request.roomId());

            return body;
        } catch (GameException e) {
            throw e;
        } catch (Exception e) {
            throw new GameException(ErrorCode.SERVICE_UNAVAILABLE, e);
        }
    }

    public TicTacToeGameMessage processMove(TicTacToeGameMessage request) {
        URI uri = UriComponentsBuilder.fromUriString(gameServiceUrl)
                .path("/game/t-t-t/move")
                .build()
                .toUri();

        log.info("Calling game-service to process move: {}", request);

        try {
            ResponseEntity<TicTacToeGameMessage> response = restTemplate.exchange(
                    new RequestEntity<>(request, HttpMethod.POST, uri),
                    TicTacToeGameMessage.class
            );

            TicTacToeGameMessage body = response.getBody();

            if (body == null) {
                throw new GameException(ErrorCode.SERVICE_UNAVAILABLE);
            }

            log.info("Move processed successfully: roomId={}", request.roomId());

            return body;
        } catch (GameException e) {
            throw e;
        } catch (Exception e) {
            throw new GameException(ErrorCode.SERVICE_UNAVAILABLE, e);
        }
    }

    // -------------------------------------------------------------------------
    // HorseRace
    // -------------------------------------------------------------------------

    public Optional<HorseRaceGameInternalResponse> createRace(HorseRaceGameInternalRequest request) {
        URI uri = UriComponentsBuilder.fromUriString(gameServiceUrl)
                .path("/game/horse-race/create")
                .build()
                .toUri();

        log.info("Calling game-service to create race preset: roomId={}", request.roomId());

        try {
            ResponseEntity<HorseRaceGameInternalResponse> response = restTemplate.exchange(
                    new RequestEntity<>(request, HttpMethod.POST, uri),
                    HorseRaceGameInternalResponse.class
            );

            log.info("Race preset created successfully: {}", response.getBody());

            return Optional.ofNullable(response.getBody());
        } catch (Exception e) {
            log.error("Failed to create race preset: {}", e.getMessage());
            throw new RuntimeException("Failed to create race preset: " + e.getMessage(), e);
        }
    }

    public Optional<HorseRaceGameInternalResponse> startRace(HorseRaceGameInternalRequest request) {
        URI uri = UriComponentsBuilder.fromUriString(gameServiceUrl)
                .path("/game/horse-race/start")
                .build()
                .toUri();

        log.info("Calling game-service to start race: roomId={}, horseCount={}", request.roomId(), request.horseCount());

        try {
            ResponseEntity<HorseRaceGameInternalResponse> response = restTemplate.exchange(
                    new RequestEntity<>(request, HttpMethod.POST, uri),
                    HorseRaceGameInternalResponse.class
            );

            log.info("Race started successfully: roomId={}, race={}", request.roomId(), response.getBody());

            return Optional.ofNullable(response.getBody());
        } catch (GameException e) {
            throw e;
        } catch (Exception e) {
            throw new GameException(ErrorCode.SERVICE_UNAVAILABLE, e);
        }
    }

    public void finishRace(HorseRaceGameInternalRequest request) {
        URI uri = UriComponentsBuilder.fromUriString(gameServiceUrl)
                .path("/game/horse-race/result")
                .build()
                .toUri();

        log.info("Calling game-service to finish race: roomId={}", request.roomId());

        try {
            restTemplate.exchange(
                    new RequestEntity<>(request, HttpMethod.POST, uri),
                    Void.class
            );

            log.info("Race finished successfully: roomId={}", request.roomId());
        } catch (Exception e) {
            log.error("Failed to finish race: {}", e.getMessage());
            throw new RuntimeException("Failed to finish race: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Durak
    // -------------------------------------------------------------------------

    public DurakGameInternalResponse startDurakGame(DurakGameInternalRequest request) {
        URI uri = UriComponentsBuilder.fromUriString(gameServiceUrl)
                .path("/game/durak/start")
                .build()
                .toUri();

        log.info("Calling game-service to start Durak game: roomId={}, players={}", request.roomId(), request.players());

        try {
            ResponseEntity<DurakGameInternalResponse> response = restTemplate.exchange(
                    new RequestEntity<>(request, HttpMethod.POST, uri),
                    DurakGameInternalResponse.class
            );

            DurakGameInternalResponse body = response.getBody();

            if (body == null) {
                throw new GameException(ErrorCode.GAME_NOT_STARTED);
            }

            log.info("Durak game started: gameId={}, roomId={}", body.id(), request.roomId());

            return body;
        } catch (GameException e) {
            throw e;
        } catch (Exception e) {
            throw new GameException(ErrorCode.SERVICE_UNAVAILABLE, e);
        }
    }

    public DurakGameInternalResponse processDurakMove(DurakGameInternalRequest request) {
        URI uri = UriComponentsBuilder.fromUriString(gameServiceUrl)
                .path("/game/durak/move")
                .build()
                .toUri();

        log.info("Calling game-service for Durak move: gameId={}, actor={}, action={}, card={}", request.id(), request.currentActorId(), request.action(), request.card());

        try {
            ResponseEntity<DurakGameInternalResponse> response = restTemplate.exchange(
                    new RequestEntity<>(request, HttpMethod.POST, uri),
                    DurakGameInternalResponse.class
            );

            DurakGameInternalResponse body = response.getBody();

            if (body == null) {
                throw new GameException(ErrorCode.SERVICE_UNAVAILABLE);
            }

            log.info("Durak move processed: gameId={}, isGameOver={}, winner={}", body.id(), body.isGameOver(), body.winnerId());

            return body;
        } catch (GameException e) {
            throw e;
        } catch (Exception e) {
            throw new GameException(ErrorCode.SERVICE_UNAVAILABLE, e);
        }
    }

    public DurakGameInternalResponse processDurakEnd(DurakGameInternalRequest request) {
        URI uri = UriComponentsBuilder.fromUriString(gameServiceUrl)
                .path("/game/durak/timeout")
                .build()
                .toUri();

        log.info("Calling game-service to finalize Durak game by timeout: gameId={}, winner={}",
                request.id(), request.winnerId());

        try {
            ResponseEntity<DurakGameInternalResponse> response = restTemplate.exchange(
                    new RequestEntity<>(request, HttpMethod.POST, uri),
                    DurakGameInternalResponse.class
            );

            DurakGameInternalResponse body = response.getBody();

            if (body == null || !body.isGameOver()) {
                throw new GameException(ErrorCode.SERVICE_UNAVAILABLE);
            }

            log.info("Durak game finalized: gameId={}", request.id());

            return body;
        } catch (Exception e) {
            log.error("Failed to finalize Durak game by timeout: gameId={}", request.id(), e);
            throw new GameException(ErrorCode.SERVICE_UNAVAILABLE, e);
        }
    }
    // De-Coder
    // -------------------------------------------------------------------------

    public Optional<DeCoderGameInternalResponse> startDeCoderGame(DeCoderGameInternalRequest request) {
        URI uri = UriComponentsBuilder.fromUriString(gameServiceUrl)
                .path("/game/de_coder/start")
                .build()
                .toUri();

        log.info("Calling game-service to start game: {}", request);

        try {
            ResponseEntity<DeCoderGameInternalResponse> response = restTemplate.exchange(
                    new RequestEntity<>(
                            request,
                            HttpMethod.POST,
                            uri
                    ),
                    DeCoderGameInternalResponse.class
            );

            log.info("Game started successfully: {}", response);

            return Optional.ofNullable(response.getBody());
        } catch (Exception e) {
            log.error("Failed to start game {}", e.getMessage());
            throw new RuntimeException("Failed to start game" + e.getMessage(), e);
        }
    }

    public Optional<DeCoderGameInternalResponse> processDeCoderMove(DeCoderGameInternalRequest request) {
        URI uri = UriComponentsBuilder.fromUriString(gameServiceUrl)
                .path("/game/de_coder/move")
                .build()
                .toUri();

        log.info("Calling game-service to process De-Coder move: {}", request);

        try {
            ResponseEntity<DeCoderGameInternalResponse> response = restTemplate.exchange(
                    new RequestEntity<>(request, HttpMethod.POST, uri),
                    DeCoderGameInternalResponse.class
            );

            log.info("De-Coder move processed successfully: {}", response.getBody());

            return Optional.ofNullable(response.getBody());

        } catch (HttpClientErrorException.TooManyRequests e) {
            assert e.getResponseHeaders() != null;
            throw new RuntimeException("COOLDOWN:" + e.getResponseHeaders().getFirst("Retry-After"));

        } catch (Exception e) {
            log.error("Failed to process De-Coder move {}", e.getMessage());
            throw new RuntimeException("Failed to process move: " + e.getMessage(), e);
        }
    }

    public Optional<DeCoderGameInternalResponse> getDeCoderGameState(UUID roomId) {
        URI uri = UriComponentsBuilder.fromUriString(gameServiceUrl)
                .path("/game/de_coder/{roomId}/state")
                .buildAndExpand(roomId)
                .toUri();

        log.info("Calling game-service to get De-Coder state: roomId={}", roomId);

        try {
            ResponseEntity<DeCoderGameInternalResponse> response = restTemplate.exchange(
                    new RequestEntity<>(HttpMethod.GET, uri),
                    DeCoderGameInternalResponse.class
            );
            log.info("De-Coder state received successfully: {}", response.getBody());
            return Optional.ofNullable(response.getBody());
        } catch (Exception e) {
            log.warn("Failed to get De-Coder game state for room {}: {}", roomId, e.getMessage());
            return Optional.empty();
        }
    }
}
