package casualgames.userservice.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID guid,

        String username,

        String email,

        BigDecimal balance,

        String role,

        String status,

        Instant createdAt
) {
}
