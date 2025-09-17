package casualgames.userservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
public class UserResponseDTO {

    private Long id;

    private String username;

    private String email;

    private BigDecimal balance;

    private Instant createdAt;
}
