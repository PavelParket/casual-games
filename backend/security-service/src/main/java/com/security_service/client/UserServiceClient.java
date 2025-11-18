package com.security_service.client;

import com.security_service.domain.dto.user_service.CreateUserRequest;
import com.security_service.domain.dto.user_service.CreateUserResponse;
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

    public CreateUserResponse create(CreateUserRequest request) {
        URI uri = UriComponentsBuilder.fromUriString(userServiceUrl)
                .path("/users")
                .build()
                .toUri();
        try {
            ResponseEntity<CreateUserResponse> response = restTemplate.exchange(
                    new RequestEntity<>(request, HttpMethod.POST, uri),
                    CreateUserResponse.class
            );

            return response.getBody();
        } catch (RestClientException e) {
            log.error("Failed to call User Service: {}", e.getMessage(), e);
            throw new ServiceUnavailableException("Service is unavailable");
        }
    }

    /*public void delete(String email) {
        URI uri = UriComponentsBuilder.fromUriString(userServiceUrl)
                .path("/users/email/{email}")
                .queryParam(email)
                .build()
                .toUri();

        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                    new RequestEntity<>(HttpMethod.DELETE, uri),
                    Void.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RestClientException("Failed to delete user, status=" + response.getStatusCode());
            }
        } catch (RestClientException e) {
            log.error("Failed to call User Service: {}", e.getMessage(), e);
            throw new ServiceUnavailableException(e.getMessage());
        }
    }*/
}
