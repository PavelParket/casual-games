package com.bank_service.client;

import com.bank_service.domain.dto.user_service.TransactionShortInfoInternalRequest;
import com.bank_service.exception.ClientInternalRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceClient {

    @Value("${app.user-service.url}")
    private String userServiceUrl;

    private final RestTemplate restTemplate;

    public void sendUpdates(List<TransactionShortInfoInternalRequest> transactions) {
        URI uri = UriComponentsBuilder.fromUriString(userServiceUrl)
                .path("/users/update-balance")
                .build()
                .toUri();

        try {
            ResponseEntity<Boolean> response = restTemplate.exchange(
                    new RequestEntity<>(transactions, HttpMethod.PATCH, uri),
                    Boolean.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new ClientInternalRequestException("User-service returned error code: " + response.getStatusCode());
            }

            if (Boolean.FALSE.equals(response.getBody())) {
                throw new ClientInternalRequestException("User-service failed to apply transactions");
            }

            log.info("User info retrieved successfully: {}", response);
        } catch (RestClientException e) {
            log.error("Error calling user-service: {}", e.getMessage(), e);
            throw new ClientInternalRequestException("Failed to update balances, service is unavailable");
        }
    }
}
