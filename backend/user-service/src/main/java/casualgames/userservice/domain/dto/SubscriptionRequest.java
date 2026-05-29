package casualgames.userservice.domain.dto;

import com.security_starter.enums.Status;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record SubscriptionRequest(

        @NotNull(message = "Status cannot be null")
        Status status
) {
}
