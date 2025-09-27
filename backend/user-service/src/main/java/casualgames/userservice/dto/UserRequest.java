package casualgames.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequest(

        @NotBlank
        @Size(min = 6, max = 50)
        String username,

        @NotBlank
        @Email
        @Size(max = 250)
        String email
) {}
