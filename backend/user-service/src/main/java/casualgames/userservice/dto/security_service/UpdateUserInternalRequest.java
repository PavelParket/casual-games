package casualgames.userservice.dto.security_service;

import jakarta.validation.constraints.Email;
import lombok.Builder;
import org.hibernate.validator.constraints.Length;

import java.util.UUID;

@Builder
public record UpdateUserInternalRequest(
        UUID guid,

        @Length(max = 50)
        String username,

        @Email
        @Length(max = 200)
        String email,

        @Length(min = 4)
        String password
) {
}
