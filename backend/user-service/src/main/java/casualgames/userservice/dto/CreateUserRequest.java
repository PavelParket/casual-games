package casualgames.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateUserRequest(
        UUID guid,

        @NotBlank(message = "Username can't be empty!")
        @Size(max = 50)
        @Pattern(regexp = "^[a-zA-Z0-9_]+$")
        String username,

        @NotBlank(message = "Email can't be empty!")
        @Email(message = "Email should satisfy the email format: youremail@gmail.com!")
        @Size(max = 200)
        String email
) {
}
