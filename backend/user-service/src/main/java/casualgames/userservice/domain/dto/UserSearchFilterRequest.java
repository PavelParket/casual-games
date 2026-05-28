package casualgames.userservice.domain.dto;

import com.security_starter.enums.Status;
import lombok.Builder;

@Builder
public record UserSearchFilterRequest(

        String username,

        Status status
) {
}
