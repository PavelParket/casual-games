package casualgames.userservice.dto.security_service;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record UpdateUserInternalResponse(
        UUID guid,
        String username,
        String email,
        String role,
        Instant createdAt
) {
}
