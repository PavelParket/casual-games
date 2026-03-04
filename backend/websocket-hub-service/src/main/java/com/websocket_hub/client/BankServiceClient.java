package com.websocket_hub.client;

import com.websocket_hub.domain.dto.client.DeCoderTransactionInternalRequest;
import com.websocket_hub.domain.dto.client.DeCoderTransactionInternalResponse;
import com.websocket_hub.domain.dto.client.HorseRaceTransactionInternalRequest;
import com.websocket_hub.domain.dto.client.HorseRaceTransactionInternalResponse;
import com.websocket_hub.domain.dto.client.TicTacToeTransactionInternalRequest;
import com.websocket_hub.domain.dto.client.TicTacToeTransactionInternalResponse;
import com.websocket_hub.exception.InfrastructureGameException;
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
                throw InfrastructureGameException.bankServiceUnexpectedStatus(
                        "sendTicTacToeGameResults",
                        request.roomId(),
                        response.getStatusCode().value()
                );
            }

            TicTacToeTransactionInternalResponse body = response.getBody();

            if (body == null) {
                throw InfrastructureGameException.bankServiceNullResponse(
                        "sendTicTacToeGameResults",
                        request.roomId()
                );
            }

            log.info("Bank-service processed t-t-t results: status={}, message={}, transactions={}", body.status(), body.message(), body.transactionsCreated());

            return body;

        } catch (InfrastructureGameException e) {
            throw e;
        } catch (Exception e) {
            throw InfrastructureGameException.bankServiceUnavailable("sendTicTacToeGameResults", request.roomId(), e);
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
                throw InfrastructureGameException.bankServiceNullResponse(
                        "sendHorseRaceGameResults",
                        request.roomId()
                );
            }

            log.info("Bank-service processed horse race results: status={}, message={}, transactions={}", body.status(), body.message(), body.transactionsCreated());

            return body;

        } catch (InfrastructureGameException e) {
            throw e;
        } catch (Exception e) {
            throw InfrastructureGameException.bankServiceUnavailable("sendHorseRaceGameResults", request.roomId(), e);
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
                throw InfrastructureGameException.bankServiceUnexpectedStatus(
                        "sendDeCoderGameTransaction",
                        request.roomId(),
                        response.getStatusCode().value()
                );
            }

            DeCoderTransactionInternalResponse body = response.getBody();

            if (body == null) {
                throw InfrastructureGameException.bankServiceNullResponse(
                        "sendDeCoderGameTransaction",
                        request.roomId()
                );
            }

            if ("FAILED".equalsIgnoreCase(body.status())) {
                throw InfrastructureGameException.bankServiceUnexpectedStatus(
                        "sendDeCoderGameTransaction",
                        request.roomId(),
                        response.getStatusCode().value()
                );
            }

            log.info("Bank-service processed De-Coder transaction: status={}, message={}", body.status(), body.message());
            return body;

        } catch (InfrastructureGameException e) {
            throw e;
        } catch (Exception e) {
            throw InfrastructureGameException.bankServiceUnavailable("sendDeCoderGameTransaction", request.roomId(), e);
        }
    }
}
