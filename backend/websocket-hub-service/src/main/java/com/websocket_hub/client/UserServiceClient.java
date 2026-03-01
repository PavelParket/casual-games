package com.websocket_hub.client;

import com.websocket_hub.domain.dto.client.UserInternalResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceClient {

    @Value("${app.user-service.url}")
    private String userServiceUrl;

    private final RestTemplate restTemplate;

    public UserInternalResponse getUserByGuid(UUID guid, String token) {
        URI uri = UriComponentsBuilder.fromUriString(userServiceUrl)
                .path("/users/guid={guid}")
                .buildAndExpand(guid)
                .toUri();

        log.info("Calling user-service to get user: {}", guid);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        try {
            ResponseEntity<UserInternalResponse> response = restTemplate.exchange(
                    new RequestEntity<>(headers, HttpMethod.GET, uri),
                    UserInternalResponse.class
            );

            log.info("User info retrieved successfully: {}", response);

            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw new RuntimeException(e.getResponseBodyAsString());
        } catch (RestClientException e) {
            log.error("Failed to call User Service: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get user: " + e.getMessage());
        }
    }
}
