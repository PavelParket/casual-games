package casualgames.userservice.domain.dto;

import com.security_starter.enums.Status;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record SubscriptionPlanResponse(

        Long id,

        Status status,

        BigDecimal price,

        BigDecimal upgradePrice,

        Integer tier
) {
}
