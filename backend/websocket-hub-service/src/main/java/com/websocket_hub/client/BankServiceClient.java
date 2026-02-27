package com.websocket_hub.client;

import com.websocket_hub.domain.dto.bank_service.DeCoderTransactionInternalRequest;
import com.websocket_hub.domain.dto.bank_service.TicTacToeTransactionInternalRequest;
import com.websocket_hub.domain.dto.bank_service.TicTacToeTransactionInternalResponse;
import com.websocket_hub.domain.dto.bank_service.DeCoderTransactionInternalResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
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

        log.info("Calling bank-service to process game results: roomId={}, winner={}", request.roomId(), request.winner());

        try {
            ResponseEntity<TicTacToeTransactionInternalResponse> response = restTemplate.exchange(
                    new RequestEntity<>(request, HttpMethod.POST, uri),
                    TicTacToeTransactionInternalResponse.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Bank-service returned error status: {}", response.getStatusCode());
                throw new RuntimeException("Bank-service returned error code: " + response.getStatusCode());
            }

            TicTacToeTransactionInternalResponse body = response.getBody();

            if (body == null) {
                log.error("Bank-service returned null body");
                throw new RuntimeException("Bank-service returned null response");
            }

            log.info("Bank-service processed results successfully: status={}, message={}, transactions={}",
                    body.status(), body.message(), body.transactionsCreated());

            return body;
        } catch (RestClientException e) {
            log.error("Failed to call bank-service: {}", e.getMessage());
            throw new RuntimeException("Failed to process game results: " + e.getMessage(), e);
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
                log.error("Bank-service returned error status: {}", response.getStatusCode());
                throw new RuntimeException("Bank-service returned error code: " + response.getStatusCode());
            }

            DeCoderTransactionInternalResponse body = response.getBody();

            if (body == null) {
                throw new RuntimeException("Bank-service returned null body");
            }

            if ("FAILED".equalsIgnoreCase(body.status())) {
                throw new RuntimeException("Transaction rejected: " + body.message());
            }

            log.info("Bank-service processed De-Coder transaction: status={}, message={}", body.status(), body.message());
            return body;

        } catch (Exception e) {
            log.error("Failed to call bank-service for De-Coder: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
}
