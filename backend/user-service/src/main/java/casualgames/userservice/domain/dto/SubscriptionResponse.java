package casualgames.userservice.domain.dto;

import com.security_starter.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubscriptionResponse {

    private Status status;

    private Instant startedAt;

    private Instant expiresAt;

    private boolean autoRenew;

    private Status newStatus;

    private Instant statusChangeAt;

    private Instant createdAt;

    private Instant updatedAt;
}
