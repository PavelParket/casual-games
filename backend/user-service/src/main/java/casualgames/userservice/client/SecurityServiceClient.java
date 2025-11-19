package casualgames.userservice.client;

import casualgames.userservice.dto.security_service.UpdateUserInternalRequest;
import casualgames.userservice.dto.security_service.UpdateUserInternalResponse;
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

    public UpdateUserInternalResponse update(UpdateUserInternalRequest request) {
        URI uri = UriComponentsBuilder.fromUriString(securityServiceUrl)
                .path("/users/{guid}")
                .buildAndExpand(request.guid())
                .toUri();

        try {
            ResponseEntity<UpdateUserInternalResponse> response = restTemplate.exchange(
                    new RequestEntity<>(request, HttpMethod.PUT, uri),
                    UpdateUserInternalResponse.class
            );

            return response.getBody();
        } catch (RestClientException e) {
            log.error("Failed to call User Service: {}", e.getMessage(), e);
            throw new ServiceUnavailableException("Service is unavailable");
        }
    }
}
