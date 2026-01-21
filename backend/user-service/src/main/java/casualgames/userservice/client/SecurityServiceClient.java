package casualgames.userservice.client;

import casualgames.userservice.dto.security_service.UpdateUserInternalRequest;
import casualgames.userservice.dto.security_service.UpdateUserInternalResponse;
import casualgames.userservice.entity.User;
import casualgames.userservice.exception.ServiceUnavailableException;
import com.security_starter.enums.Role;
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
import java.util.Optional;
import java.util.UUID;

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
                .path("/users/guid={guid}")
                .buildAndExpand(request.guid())
                .toUri();

        try {
            ResponseEntity<UpdateUserInternalResponse> response = restTemplate.exchange(
                    new RequestEntity<>(request, HttpMethod.PUT, uri),
                    UpdateUserInternalResponse.class
            );

            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw new ServiceUnavailableException(e.getResponseBodyAsString());
        } catch (RestClientException e) {
            log.error("Failed to call User Service: {}", e.getMessage(), e);
            throw new ServiceUnavailableException("Failed to update user: " + e.getMessage());
        }
    }

    public void delete(UUID guid) {
        URI uri = UriComponentsBuilder.fromUriString(securityServiceUrl)
                .path("/users/guid={guid}")
                .buildAndExpand(guid)
                .toUri();

        try {
            restTemplate.exchange(new RequestEntity<>(HttpMethod.DELETE, uri), Void.class);
        } catch (HttpClientErrorException e) {
            throw new ServiceUnavailableException(e.getResponseBodyAsString());
        } catch (RestClientException e) {
            log.error("Failed to call User Service: {}", e.getMessage(), e);
            throw new ServiceUnavailableException("Failed to delete user: " + e.getMessage());
        }
    }

    public void updateRole(User author, User target, Role role) {
        URI uri = UriComponentsBuilder.fromUriString(securityServiceUrl)
                .path("/users/update-role")
                .queryParam("author", author)
                .queryParam("guid", target.getGuid())
                .queryParam("role", role)
                .build()
                .toUri();

        try {
            ResponseEntity<UpdateUserInternalResponse> response = restTemplate.exchange(
                    new RequestEntity<>(HttpMethod.PUT, uri),
                    UpdateUserInternalResponse.class
            );

            if (Optional.ofNullable(response.getBody()).isEmpty()) {
                throw new RestClientException("Failed to update user role");
            }
        } catch (HttpClientErrorException e) {
            throw new ServiceUnavailableException(e.getResponseBodyAsString());
        } catch (RestClientException e) {
            log.error("Failed to call User Service: {}", e.getMessage());
            throw new ServiceUnavailableException("Failed to update user: " + e.getMessage());
        }
    }
}
