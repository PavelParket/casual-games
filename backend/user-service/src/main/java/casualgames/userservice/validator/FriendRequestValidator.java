package casualgames.userservice.validator;

import casualgames.userservice.domain.entity.FriendRequest;
import com.common_utils.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static casualgames.userservice.config.ResourceMessageConstants.CONFLICT_REQUEST_COOLDOWN;
import static casualgames.userservice.service.FriendRequestService.REQUEST_COOLDOWN_DAYS;

@Component
@RequiredArgsConstructor
@Slf4j
public class FriendRequestValidator {

    public void validateCooldown(FriendRequest friendRequest) {
        if (friendRequest.getResolvedAt()
                .plus(REQUEST_COOLDOWN_DAYS, ChronoUnit.DAYS)
                .isAfter(Instant.now())) {
            throw new ConflictException(CONFLICT_REQUEST_COOLDOWN);
        }
    }
}
