package com.security_service.client;

import com.security_service.domain.dto.user_service.CreateUserInternalRequest;
import com.security_service.domain.dto.user_service.CreateUserInternalResponse;
import com.security_service.exception.ServiceUnavailableException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserServiceClient {

    @Value("${app.user-service.url}")
    @NonFinal
    String userServiceUrl;

    RestTemplate restTemplate;

    public CreateUserInternalResponse create(CreateUserInternalRequest request) {
        URI uri = UriComponentsBuilder.fromUriString(userServiceUrl)
                .path("/users")
                .build()
                .toUri();
        try {
            ResponseEntity<CreateUserInternalResponse> response = restTemplate.exchange(
                    new RequestEntity<>(request, HttpMethod.POST, uri),
                    CreateUserInternalResponse.class
            );

            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw new ServiceUnavailableException(e.getResponseBodyAsString());
        } catch (RestClientException e) {
            log.error("Failed to call User Service: {}", e.getMessage(), e);
            throw new ServiceUnavailableException("Failed to create user: " + e.getMessage());
        }
    }
}
