package casualgames.userservice.service;

import casualgames.userservice.domain.dto.SubscriptionPlanResponse;
import casualgames.userservice.domain.entity.SubscriptionPlan;
import casualgames.userservice.domain.entity.User;
import casualgames.userservice.domain.entity.UserSubscription;
import casualgames.userservice.repository.SubscriptionPlanRepository;
import casualgames.userservice.repository.UserRepository;
import casualgames.userservice.repository.UserSubscriptionRepository;
import casualgames.userservice.service.helper.PermissionHelper;
import casualgames.userservice.service.helper.SubscriptionHelper;
import com.common_utils.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static casualgames.userservice.config.ResourceMessageConstants.NOT_FOUND_SUBSCRIPTION_PLAN;
import static casualgames.userservice.config.ResourceMessageConstants.NOT_FOUND_USER;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionPlanService {

    private final SubscriptionPlanRepository subscriptionPlanRepository;

    private final UserRepository userRepository;

    private final UserSubscriptionRepository userSubscriptionRepository;

    private final SubscriptionHelper subscriptionHelper;

    private final PermissionHelper permissionHelper;

    @Transactional(readOnly = true)
    public List<SubscriptionPlanResponse> get() {
        UUID userGuid = permissionHelper.getToken().getGuid();

        User user = userRepository.findByGuid(userGuid)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_USER, userGuid)));

        SubscriptionPlan currentPlan = subscriptionPlanRepository.findByStatus(user.getStatus())
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_SUBSCRIPTION_PLAN, user.getStatus())));

        UserSubscription subscription = userSubscriptionRepository.findByUserGuid(userGuid)
                .orElse(null);

        Instant now = Instant.now();

        return subscriptionPlanRepository.findAll()
                .stream()
                .map(plan -> buildResponse(plan, currentPlan, subscription, now))
                .toList();
    }

    private SubscriptionPlanResponse buildResponse(SubscriptionPlan targetPlan, SubscriptionPlan currentPlan, UserSubscription subscription, Instant now) {
        return SubscriptionPlanResponse.builder()
                .id(targetPlan.getId())
                .status(targetPlan.getStatus())
                .price(targetPlan.getPrice())
                .upgradePrice(resolveUpgradePrice(targetPlan, currentPlan, subscription, now))
                .tier(targetPlan.getTier())
                .build();
    }

    private BigDecimal resolveUpgradePrice(SubscriptionPlan targetPlan, SubscriptionPlan currentPlan, UserSubscription subscription, Instant now) {
        if (targetPlan.getTier() <= currentPlan.getTier()) {
            return null;
        }

        if (currentPlan.getTier() == 0 || subscription == null) {
            return targetPlan.getPrice();
        }

        return subscriptionHelper.calculateUpgradeAmount(subscription, currentPlan, targetPlan, now);
    }
}
