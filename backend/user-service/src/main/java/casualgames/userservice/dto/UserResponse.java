package casualgames.userservice.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record UserResponse(
        Long id,
        String username,
        String email,
        BigDecimal balance,
        Instant createdAt
) {}
