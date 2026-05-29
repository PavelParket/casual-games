package casualgames.userservice.service;

import casualgames.userservice.domain.dto.SubscriptionRequest;
import casualgames.userservice.domain.dto.SubscriptionResponse;
import casualgames.userservice.domain.entity.SubscriptionPlan;
import casualgames.userservice.domain.entity.User;
import casualgames.userservice.domain.entity.UserSubscription;
import casualgames.userservice.mapper.SubscriptionMapper;
import casualgames.userservice.repository.SubscriptionPlanRepository;
import casualgames.userservice.repository.UserRepository;
import casualgames.userservice.repository.UserSubscriptionRepository;
import casualgames.userservice.service.helper.KafkaMessageHelper;
import casualgames.userservice.service.helper.PermissionHelper;
import casualgames.userservice.service.helper.SubscriptionHelper;
import com.common_utils.exception.BadRequestException;
import com.common_utils.exception.ConflictException;
import com.common_utils.exception.ForbiddenException;
import com.common_utils.exception.NotFoundException;
import com.security_starter.config.AuthenticationToken;
import com.security_starter.enums.Operation;
import com.security_starter.enums.Permissions;
import com.security_starter.enums.Status;
import com.security_starter.validator.PermissionValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static casualgames.userservice.config.ResourceMessageConstants.BAD_REQUEST_NO_NECESSARY_BALANCE_AMOUNT;
import static casualgames.userservice.config.ResourceMessageConstants.CONFLICT_SAME_TIER_SUBSCRIPTION;
import static casualgames.userservice.config.ResourceMessageConstants.DO_NOT_HAVE_PERMISSION_TO_READ_SUBSCRIPTION;
import static casualgames.userservice.config.ResourceMessageConstants.DO_NOT_HAVE_PERMISSION_TO_UPDATE_SUBSCRIPTION;
import static casualgames.userservice.config.ResourceMessageConstants.NOT_FOUND_SUBSCRIPTION;
import static casualgames.userservice.config.ResourceMessageConstants.NOT_FOUND_SUBSCRIPTION_PLAN;
import static casualgames.userservice.config.ResourceMessageConstants.NOT_FOUND_USER;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSubscriptionService {

    private static final String SUBSCRIPTION_UPGRADE = "SUBSCRIPTION_UPGRADE";

    private final UserRepository userRepository;

    private final SubscriptionPlanRepository subscriptionPlanRepository;

    private final UserSubscriptionRepository userSubscriptionRepository;

    private final SubscriptionMapper subscriptionMapper;

    private final SubscriptionHelper subscriptionHelper;

    private final KafkaMessageHelper kafkaMessageHelper;

    private final PermissionHelper permissionHelper;

    private final PermissionValidator permissionValidator;

    @Transactional
    public SubscriptionResponse purchase(SubscriptionRequest request) {
        AuthenticationToken token = permissionHelper.getToken();

        if (!permissionValidator.can(
                Permissions.SUBSCRIPTION,
                Operation.UPDATE,
                permissionHelper.getContext(token.getGuid()),
                token
        )) {
            throw new ForbiddenException(DO_NOT_HAVE_PERMISSION_TO_UPDATE_SUBSCRIPTION);
        }

        User user = userRepository.findByGuidForUpdate(token.getGuid())
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_USER, token.getGuid())));

        SubscriptionPlan currentPlan = subscriptionPlanRepository.findByStatus(user.getStatus())
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_SUBSCRIPTION_PLAN, user.getStatus())));

        SubscriptionPlan targetPlan = subscriptionPlanRepository.findByStatus(request.status())
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_SUBSCRIPTION_PLAN, request.status())));

        if (currentPlan.getTier().equals(targetPlan.getTier())) {
            throw new ConflictException(String.format(CONFLICT_SAME_TIER_SUBSCRIPTION, request.status()));
        }

        UserSubscription currentSubscription = userSubscriptionRepository.findByUserGuid(token.getGuid())
                .orElse(
                        UserSubscription.builder()
                                .userGuid(token.getGuid())
                                .build()
                );

        return currentPlan.getTier() > targetPlan.getTier()
                ? downgrade(request.status(), currentSubscription, user)
                : upgrade(user, currentSubscription, currentPlan, targetPlan, Instant.now());
    }

    private SubscriptionResponse upgrade(User user,
                                         UserSubscription subscription,
                                         SubscriptionPlan currentPlan,
                                         SubscriptionPlan targetPlan,
                                         Instant date) {
        BigDecimal amount;

        if (currentPlan.getTier() == 0) {
            amount = targetPlan.getPrice();
        } else {
            amount = subscriptionHelper.calculateUpgradeAmount(subscription, currentPlan, targetPlan, date);
        }

        BigDecimal balanceBefore = user.getBalance();
        BigDecimal balanceAfter = balanceBefore.subtract(amount);

        if (balanceAfter.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException(BAD_REQUEST_NO_NECESSARY_BALANCE_AMOUNT);
        }

        Status targetStatus = targetPlan.getStatus();

        user.setBalance(balanceAfter);
        user.setStatus(targetStatus);
        userRepository.save(user);

        subscription.setStartedAt(date);
        subscription.setExpiresAt(date.plus(SubscriptionHelper.SUBSCRIPTION_PERIOD_DAYS, ChronoUnit.DAYS));
        subscription.setNewStatus(null);
        subscription.setStatusChangeAt(null);

        userSubscriptionRepository.save(subscription);

        kafkaMessageHelper.save(
                kafkaMessageHelper.getTopics().getUpdateSubscription(),
                kafkaMessageHelper.buildUpdateSubscriptionEvent(user.getGuid(), SUBSCRIPTION_UPGRADE, amount, balanceBefore, balanceAfter)
        );

        return subscriptionMapper.toResponse(subscription, targetStatus);
    }

    private SubscriptionResponse downgrade(Status targetStatus,
                                           UserSubscription currentSubscription,
                                           User user) {
        currentSubscription.setNewStatus(targetStatus);
        currentSubscription.setStatusChangeAt(currentSubscription.getExpiresAt());
        userSubscriptionRepository.save(currentSubscription);

        return subscriptionMapper.toResponse(currentSubscription, user.getStatus());
    }

    @Transactional(readOnly = true)
    public SubscriptionResponse get() {
        AuthenticationToken token = permissionHelper.getToken();

        if (!permissionValidator.can(
                Permissions.SUBSCRIPTION,
                Operation.READ,
                permissionHelper.getContext(token.getGuid()),
                token
        )) {
            throw new ForbiddenException(DO_NOT_HAVE_PERMISSION_TO_READ_SUBSCRIPTION);
        }

        User user = userRepository.findByGuid(token.getGuid())
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_USER, token.getGuid())));

        UserSubscription subscription = userSubscriptionRepository.findByUserGuid(token.getGuid())
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_SUBSCRIPTION, token.getGuid())));

        return subscriptionMapper.toResponse(subscription, user.getStatus());
    }

    @Transactional
    public SubscriptionResponse updateAutoRenew(Boolean enable) {
        AuthenticationToken token = permissionHelper.getToken();

        if (!permissionValidator.can(
                Permissions.SUBSCRIPTION,
                Operation.UPDATE,
                permissionHelper.getContext(token.getGuid()),
                token
        )) {
            throw new ForbiddenException(DO_NOT_HAVE_PERMISSION_TO_UPDATE_SUBSCRIPTION);
        }

        UserSubscription subscription = userSubscriptionRepository.findByUserGuid(token.getGuid())
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_SUBSCRIPTION, token.getGuid())));

        subscription.setAutoRenew(enable);
        userSubscriptionRepository.save(subscription);

        User user = userRepository.findByGuid(token.getGuid())
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_USER, token.getGuid())));

        return subscriptionMapper.toResponse(subscription, user.getStatus());
    }

    @Transactional
    public void processOne(UserSubscription subscription, Instant now) {
        if (subscription == null) {
            return;
        }

        try {
            boolean hasChange = subscription.getNewStatus() != null
                    && subscription.getStatusChangeAt() != null
                    && !subscription.getStatusChangeAt().isAfter(now);

            if (hasChange) {
                handleScheduledChange(subscription, now);
            } else if (!subscription.getExpiresAt().isAfter(now)) {
                handleExpiry(subscription, now);
            }
        } catch (Exception e) {
            log.error("Unexpected error processing subscription for userGuid={}", subscription.getUserGuid(), e);
        }
    }

    private void handleExpiry(UserSubscription subscription, Instant now) {
        UUID userGuid = subscription.getUserGuid();

        if (!subscription.isAutoRenew()) {
            resetToDefault(userGuid);
            return;
        }

        User user = userRepository.findByGuidForUpdate(userGuid)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_USER, userGuid)));

        SubscriptionPlan plan = subscriptionPlanRepository.findByStatus(user.getStatus())
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_SUBSCRIPTION_PLAN, user.getStatus())));

        if (plan.getTier() == 0) {
            return;
        }

        chargeAndRenew(subscription, user, user.getStatus(), plan.getPrice(), now);
    }

    private void handleScheduledChange(UserSubscription subscription, Instant now) {
        UUID userGuid = subscription.getUserGuid();
        Status targetStatus = subscription.getNewStatus();

        SubscriptionPlan targetPlan = subscriptionPlanRepository.findByStatus(targetStatus)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_SUBSCRIPTION_PLAN, targetStatus)));

        if (targetPlan.getTier() == 0) {
            resetToDefault(userGuid);
            return;
        }

        User user = userRepository.findByGuidForUpdate(userGuid)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_USER, userGuid)));

        chargeAndRenew(subscription, user, targetStatus, targetPlan.getPrice(), now);
    }

    private void chargeAndRenew(UserSubscription subscription, User user, Status targetStatus, BigDecimal price, Instant now) {
        UUID userGuid = subscription.getUserGuid();

        BigDecimal balanceBefore = user.getBalance();
        BigDecimal balanceAfter = balanceBefore.subtract(price);

        if (balanceAfter.compareTo(BigDecimal.ZERO) < 0) {
            resetToDefault(userGuid);
            return;
        }

        user.setBalance(balanceAfter);
        user.setStatus(targetStatus);
        userRepository.save(user);

        subscription.setStartedAt(now);
        subscription.setExpiresAt(now.plus(SubscriptionHelper.SUBSCRIPTION_PERIOD_DAYS, ChronoUnit.DAYS));
        subscription.setNewStatus(null);
        subscription.setStatusChangeAt(null);
        userSubscriptionRepository.save(subscription);

        kafkaMessageHelper.save(
                kafkaMessageHelper.getTopics().getUpdateSubscription(),
                kafkaMessageHelper.buildUpdateSubscriptionEvent(userGuid, SUBSCRIPTION_UPGRADE, price, balanceBefore, balanceAfter)
        );
    }

    private void resetToDefault(UUID userGuid) {
        userRepository.updateStatus(userGuid, Status.DEFAULT.name());
    }

    @Transactional(readOnly = true)
    public Page<UserSubscription> findExpiringOrScheduled(Instant now, PageRequest pageRequest) {
        return userSubscriptionRepository.findExpiringOrScheduled(now, pageRequest);
    }
}
