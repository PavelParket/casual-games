package casualgames.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponseDto {

    private Long id;

    private UUID guid;

    private String username;

    private String email;

    private BigDecimal balance;

    private String role;

    private String status;

    private Instant createdAt;
}
