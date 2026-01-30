package com.websocket_hub.client;

import com.websocket_hub.domain.dto.bank_service.TicTacToeTransactionInternalRequest;
import com.websocket_hub.domain.dto.bank_service.TicTacToeTransactionInternalResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
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

    public TicTacToeTransactionInternalResponse sendTicTacToeGameResults(TicTacToeTransactionInternalRequest request, String token) {
        URI uri = UriComponentsBuilder.fromUriString(bankServiceUrl)
                .path("/bank/save")
                .build()
                .toUri();

        log.info("Calling bank-service to process game results: roomId={}, winner={}", request.roomId(), request.winner());

        //todo: token
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        try {
            ResponseEntity<TicTacToeTransactionInternalResponse> response = restTemplate.exchange(
                    new RequestEntity<>(request, headers, HttpMethod.POST, uri),
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
}
