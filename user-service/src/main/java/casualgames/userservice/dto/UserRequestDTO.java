package casualgames.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequestDTO {

    @NotBlank
    @Size(min = 6, max = 50)
    private String username;

    @NotBlank
    @Email
    @Size(max = 250)
    private String email;

    @NotBlank
    @Pattern(regexp = "^[A-Za-z0-9]+$")
    @Size(min = 8, max = 60)
    private String password;
}
