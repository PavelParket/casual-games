package casualgames.userservice.client;

import casualgames.userservice.dto.security_service.UpdateUserRequest;
import casualgames.userservice.dto.security_service.UpdateUserResponse;
import casualgames.userservice.exception.ServiceUnavailableException;
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
public class SecurityServiceClient {

    @Value("${app.security-service.url}")
    @NonFinal
    String securityServiceUrl;

    RestTemplate restTemplate;

    public UpdateUserResponse update(UpdateUserRequest request) {
        URI uri = UriComponentsBuilder.fromUriString(securityServiceUrl)
                .path("/users")
                .build()
                .toUri();

        try {
            ResponseEntity<UpdateUserResponse> response = restTemplate.exchange(
                    new RequestEntity<>(request, HttpMethod.PUT, uri),
                    UpdateUserResponse.class
            );

            return response.getBody();
        } catch (RestClientException e) {
            log.error("Failed to call User Service: {}", e.getMessage(), e);
            throw new ServiceUnavailableException("Service is unavailable");
        }
    }
}
