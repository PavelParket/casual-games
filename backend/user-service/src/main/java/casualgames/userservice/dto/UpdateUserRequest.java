package casualgames.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Length;

public record UpdateUserRequest(
        @Size(max = 50)
        @Pattern(regexp = "^[a-zA-Z0-9_]+$")
        String username,

        @Email(message = "Email should satisfy the email format: youremail@gmail.com!")
        @Size(max = 200)
        String email,

        @Length(min = 4)
        String password
) {
}
