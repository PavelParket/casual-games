package com.casualgames.userservice.service;

import com.casualgames.commonutils.exception.NotFoundException;
import com.casualgames.securitystarter.config.AuthenticationToken;
import com.casualgames.userservice.domain.dto.SubscriptionPlanResponse;
import com.casualgames.userservice.domain.entity.SubscriptionPlan;
import com.casualgames.userservice.domain.entity.User;
import com.casualgames.userservice.domain.entity.UserSubscription;
import com.casualgames.userservice.repository.SubscriptionPlanRepository;
import com.casualgames.userservice.repository.UserRepository;
import com.casualgames.userservice.repository.UserSubscriptionRepository;
import com.casualgames.userservice.service.helper.SubscriptionHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.casualgames.userservice.config.ResourceMessageConstants.NOT_FOUND_SUBSCRIPTION_PLAN;
import static com.casualgames.userservice.config.ResourceMessageConstants.NOT_FOUND_USER;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionPlanService {

    private final SubscriptionPlanRepository subscriptionPlanRepository;

    private final UserRepository userRepository;

    private final UserSubscriptionRepository userSubscriptionRepository;

    private final SubscriptionHelper subscriptionHelper;

    @Transactional(readOnly = true)
    public List<SubscriptionPlanResponse> get(AuthenticationToken token) {
        UUID userGuid = token.getGuid();

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
