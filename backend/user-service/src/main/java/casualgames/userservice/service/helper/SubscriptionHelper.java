package casualgames.userservice.service.helper;

import casualgames.userservice.domain.entity.SubscriptionPlan;
import casualgames.userservice.domain.entity.UserSubscription;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;

@Component
public class SubscriptionHelper {

    public static final int SUBSCRIPTION_PERIOD_DAYS = 30;

    private static final BigDecimal P_MIN = new BigDecimal("0.1");
    private static final BigDecimal P_MAX = new BigDecimal("0.9");
    private static final int CALC_SCALE = 10;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    public BigDecimal calculateUpgradeAmount(
            UserSubscription current,
            SubscriptionPlan currentPlan,
            SubscriptionPlan targetPlan,
            Instant now) {

        long totalPeriodDays = ceilDays(current.getStartedAt(), current.getExpiresAt());
        long remainingDays = Math.max(0, ceilDays(now, current.getExpiresAt()));
        long daysUsed = totalPeriodDays - remainingDays;

        BigDecimal fractionUsed = BigDecimal.valueOf(daysUsed)
                .divide(BigDecimal.valueOf(totalPeriodDays), CALC_SCALE, ROUNDING);

        BigDecimal p = P_MAX.subtract(P_MAX.subtract(P_MIN).multiply(fractionUsed));

        BigDecimal dailyRate = currentPlan.getPrice()
                .divide(BigDecimal.valueOf(totalPeriodDays), CALC_SCALE, ROUNDING);

        BigDecimal credit = dailyRate
                .multiply(BigDecimal.valueOf(remainingDays))
                .multiply(p);

        return targetPlan.getPrice()
                .subtract(credit)
                .max(BigDecimal.ZERO)
                .setScale(2, ROUNDING);
    }

    private long ceilDays(Instant from, Instant to) {
        long seconds = Duration.between(from, to).toSeconds();
        return Math.max(1L, Math.ceilDiv(seconds, 86400L));
    }
}
