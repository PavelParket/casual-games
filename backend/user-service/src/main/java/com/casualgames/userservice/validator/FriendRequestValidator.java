package com.casualgames.userservice.validator;

import com.casualgames.commonutils.exception.ConflictException;
import com.casualgames.userservice.domain.entity.FriendRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static com.casualgames.userservice.config.ResourceMessageConstants.CONFLICT_REQUEST_COOLDOWN;
import static com.casualgames.userservice.service.FriendRequestService.REQUEST_COOLDOWN_DAYS;

@Component
@RequiredArgsConstructor
@Slf4j
public class FriendRequestValidator {

    public void validateCooldown(FriendRequest friendRequest) {
        Instant cooldownExpiring = friendRequest.getResolvedAt().plus(REQUEST_COOLDOWN_DAYS, ChronoUnit.DAYS);

        if (cooldownExpiring.isAfter(Instant.now())) {
            throw new ConflictException(String.format(
                    CONFLICT_REQUEST_COOLDOWN,
                    formatRemaining(Duration.between(Instant.now(), cooldownExpiring))
            ));
        }
    }

    private String formatRemaining(Duration duration) {
        long totalMinutes = Math.max(duration.toMinutes(), 1);
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;

        if (hours > 0) {
            return String.format("%dh:%dmin", hours, minutes);
        }

        return String.format("%dmin", minutes);
    }
}
