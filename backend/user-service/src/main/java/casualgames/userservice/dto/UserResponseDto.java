package casualgames.userservice.dto;

import casualgames.userservice.annotation.Permission;
import casualgames.userservice.enums.Permissions;
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

    @Permission(Permissions.GUID)
    private UUID guid;

    @Permission(Permissions.USERNAME)
    private String username;

    @Permission(Permissions.EMAIL)
    private String email;

    @Permission(Permissions.BALANCE)
    private BigDecimal balance;

    @Permission(Permissions.ROLE)
    private String role;

    @Permission(Permissions.STATUS)
    private String status;

    private Instant createdAt;
}
