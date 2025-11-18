package casualgames.userservice.dto.security_service;

import lombok.Builder;

import java.time.Instant;

@Builder
public record UpdateUserResponse(
        Long id,
        String username,
        String email,
        String role,
        Instant createdAt
) {
}
