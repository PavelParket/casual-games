package com.websocket_hub.client;

import com.websocket_hub.domain.dto.client.DeCoderTransactionInternalRequest;
import com.websocket_hub.domain.dto.client.DeCoderTransactionInternalResponse;
import com.websocket_hub.domain.dto.client.DurakTransactionInternalRequest;
import com.websocket_hub.domain.dto.client.DurakTransactionInternalResponse;
import com.websocket_hub.domain.dto.client.HorseRaceTransactionInternalRequest;
import com.websocket_hub.domain.dto.client.HorseRaceTransactionInternalResponse;
import com.websocket_hub.domain.dto.client.TicTacToeTransactionInternalRequest;
import com.websocket_hub.domain.dto.client.TicTacToeTransactionInternalResponse;
import com.websocket_hub.domain.enums.ErrorCode;
import com.websocket_hub.exception.GameException;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class BankServiceClient {

    @Value("${app.bank-service.url}")
    private String bankServiceUrl;

    private final RestTemplate restTemplate;

    public TicTacToeTransactionInternalResponse sendTicTacToeGameResults(TicTacToeTransactionInternalRequest request) {
        URI uri = UriComponentsBuilder.fromUriString(bankServiceUrl)
                .path("/bank/save")
                .build()
                .toUri();

        log.info("Calling bank-service to process tic-tac-toe game results: roomId={}, winner={}", request.roomId(), request.winner());

        try {
            ResponseEntity<TicTacToeTransactionInternalResponse> response = restTemplate.exchange(
                    new RequestEntity<>(request, HttpMethod.POST, uri),
                    TicTacToeTransactionInternalResponse.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new GameException(ErrorCode.SERVICE_UNAVAILABLE);
            }

            TicTacToeTransactionInternalResponse body = response.getBody();

            if (body == null) {
                throw new GameException(ErrorCode.SERVICE_UNAVAILABLE);
            }

            log.info("Bank-service processed t-t-t results: status={}, message={}, transactions={}", body.status(), body.message(), body.transactionsCreated());

            return body;

        } catch (GameException e) {
            throw e;
        } catch (Exception e) {
            throw new GameException(ErrorCode.SERVICE_UNAVAILABLE, e);
        }
    }

    public HorseRaceTransactionInternalResponse sendHorseRaceGameResults(HorseRaceTransactionInternalRequest request) {
        URI uri = UriComponentsBuilder.fromUriString(bankServiceUrl)
                .path("/bank/save")
                .build()
                .toUri();

        log.info("Calling bank-service to process horse race results: roomId={}, winnerHorseIndex={}, betsCount={}", request.roomId(), request.winnerHorseIndex(), request.playerBets().size());

        try {
            ResponseEntity<HorseRaceTransactionInternalResponse> response = restTemplate.exchange(
                    new RequestEntity<>(request, HttpMethod.POST, uri),
                    HorseRaceTransactionInternalResponse.class
            );

            HorseRaceTransactionInternalResponse body = response.getBody();

            if (body == null) {
                throw new GameException(ErrorCode.SERVICE_UNAVAILABLE);
            }

            log.info("Bank-service processed horse race results: status={}, message={}, transactions={}", body.status(), body.message(), body.transactionsCreated());

            return body;

        } catch (GameException e) {
            throw e;
        } catch (Exception e) {
            throw new GameException(ErrorCode.SERVICE_UNAVAILABLE, e);
        }
    }

    public DeCoderTransactionInternalResponse sendDeCoderGameTransaction(DeCoderTransactionInternalRequest request) {
        URI uri = UriComponentsBuilder.fromUriString(bankServiceUrl)
                .path("/bank/save")
                .build()
                .toUri();

        log.info("Calling bank-service to process game results: roomId={}, winner={}", request.roomId(), request.winner());

        try {
            ResponseEntity<DeCoderTransactionInternalResponse> response = restTemplate.exchange(
                    new RequestEntity<>(request, HttpMethod.POST, uri),
                    DeCoderTransactionInternalResponse.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new GameException(ErrorCode.SERVICE_UNAVAILABLE);
            }

            DeCoderTransactionInternalResponse body = response.getBody();

            if (body == null) {
                throw new GameException(ErrorCode.SERVICE_UNAVAILABLE);
            }

            if ("FAILED".equalsIgnoreCase(body.status())) {
                throw new GameException(ErrorCode.SERVICE_UNAVAILABLE);
            }

            log.info("Bank-service processed De-Coder transaction: status={}, message={}", body.status(), body.message());
            return body;

        } catch (GameException e) {
            throw e;
        } catch (Exception e) {
            throw new GameException(ErrorCode.SERVICE_UNAVAILABLE, e);
        }
    }

    public DurakTransactionInternalResponse sendDurakGameResults(DurakTransactionInternalRequest request) {
        URI uri = UriComponentsBuilder.fromUriString(bankServiceUrl)
                .path("/bank/save")
                .build()
                .toUri();

        log.info("Calling bank-service to process Durak game results: roomId={}, winner={}", request.roomId(), request.winner());

        try {
            ResponseEntity<DurakTransactionInternalResponse> response = restTemplate.exchange(
                    new RequestEntity<>(request, HttpMethod.POST, uri),
                    DurakTransactionInternalResponse.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new GameException(ErrorCode.SERVICE_UNAVAILABLE);
            }

            DurakTransactionInternalResponse body = response.getBody();

            if (body == null) {
                throw new GameException(ErrorCode.SERVICE_UNAVAILABLE);
            }

            log.info("Bank-service processed Durak results: status={}, message={}, transactions={}", body.status(), body.message(), body.transactionsCreated());

            return body;

        } catch (GameException e) {
            throw e;
        } catch (Exception e) {
            throw new GameException(ErrorCode.SERVICE_UNAVAILABLE, e);
        }
    }
}
