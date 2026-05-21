package casualgames.userservice.service;

import casualgames.userservice.dto.SubscriptionRequest;
import casualgames.userservice.dto.SubscriptionResponse;
import casualgames.userservice.entity.SubscriptionPlan;
import casualgames.userservice.entity.User;
import casualgames.userservice.entity.UserSubscription;
import casualgames.userservice.mapper.SubscriptionMapper;
import casualgames.userservice.repository.SubscriptionPlanRepository;
import casualgames.userservice.repository.UserRepository;
import casualgames.userservice.repository.UserSubscriptionRepository;
import casualgames.userservice.service.grpc.client.GrpcBankClient;
import casualgames.userservice.service.helper.PermissionHelper;
import casualgames.userservice.service.helper.SubscriptionHelper;
import com.casualgames.grpc.subscription.SubscriptionTransactionRequest;
import com.common_utils.exception.BadRequestException;
import com.common_utils.exception.ConflictException;
import com.common_utils.exception.NotFoundException;
import com.security_starter.enums.Status;
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
import static casualgames.userservice.config.ResourceMessageConstants.NOT_FOUND_SUBSCRIPTION;
import static casualgames.userservice.config.ResourceMessageConstants.NOT_FOUND_SUBSCRIPTION_PLAN;
import static casualgames.userservice.config.ResourceMessageConstants.NOT_FOUND_USER;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSubscriptionService {

    private final UserRepository userRepository;

    private final SubscriptionPlanRepository subscriptionPlanRepository;

    private final UserSubscriptionRepository userSubscriptionRepository;

    private final SubscriptionMapper subscriptionMapper;

    private final SubscriptionHelper subscriptionHelper;

    private final GrpcBankClient grpcBankClient;

    private final PermissionHelper permissionHelper;

    @Transactional
    public SubscriptionResponse purchase(SubscriptionRequest request) {
        UUID userGuid = permissionHelper.getToken().getGuid();

        User user = userRepository.findByGuid(userGuid)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_USER, userGuid)));

        SubscriptionPlan currentPlan = subscriptionPlanRepository.findByStatus(user.getStatus())
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_SUBSCRIPTION_PLAN, user.getStatus())));

        SubscriptionPlan targetPlan = subscriptionPlanRepository.findByStatus(request.status())
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_SUBSCRIPTION_PLAN, request.status())));

        if (targetPlan.getPrice().compareTo(user.getBalance()) > 0) {
            throw new BadRequestException(BAD_REQUEST_NO_NECESSARY_BALANCE_AMOUNT);
        }

        if (currentPlan.getTier().equals(targetPlan.getTier())) {
            throw new ConflictException(String.format(CONFLICT_SAME_TIER_SUBSCRIPTION, request.status()));
        }

        UserSubscription subscription = userSubscriptionRepository.findByUserGuid(userGuid)
                .orElse(
                        UserSubscription.builder()
                                .userGuid(userGuid)
                                .build()
                );

        return currentPlan.getTier() > targetPlan.getTier()
                ? downgrade(request.status(), subscription, user.getStatus())
                : upgrade(request.status(), userGuid, subscription, currentPlan, targetPlan, Instant.now());
    }

    private SubscriptionResponse upgrade(Status targetStatus,
                                         UUID userGuid,
                                         UserSubscription subscription,
                                         SubscriptionPlan currentPlan,
                                         SubscriptionPlan targetPlan,
                                         Instant date) {
        BigDecimal amount;
        String transactionType;

        if (currentPlan.getTier() == 0) {
            amount = targetPlan.getPrice();
            transactionType = "SUBSCRIPTION_PURCHASE";
        } else {
            amount = subscriptionHelper.calculateUpgradeAmount(subscription, currentPlan, targetPlan, date);
            transactionType = "SUBSCRIPTION_UPGRADE";
        }

        SubscriptionTransactionRequest request = SubscriptionTransactionRequest.newBuilder()
                .setUserGuid(userGuid.toString())
                .setAmount(amount.toPlainString())
                .setType(transactionType)
                .build();

        grpcBankClient.charge(request);

        try {
            subscription.setStartedAt(date);
            subscription.setExpiresAt(date.plus(SubscriptionHelper.SUBSCRIPTION_PERIOD_DAYS, ChronoUnit.DAYS));
            subscription.setNewStatus(null);
            subscription.setStatusChangeAt(null);
            userSubscriptionRepository.save(subscription);
            userRepository.updateStatus(userGuid, targetStatus.name());

            log.info("{} completed for user: {}, amount: {}", transactionType, userGuid, amount);

            return subscriptionMapper.toResponse(subscription, targetStatus);
        } catch (Exception e) {
            log.error("CRITICAL: bank charged user {} ({} {}) but save failed. Manual intervention required.", userGuid, transactionType, amount, e);
            throw e;
        }
    }

    private SubscriptionResponse downgrade(Status targetStatus,
                                           UserSubscription existing,
                                           Status currentStatus) {
        existing.setNewStatus(targetStatus);
        existing.setStatusChangeAt(existing.getExpiresAt());
        userSubscriptionRepository.save(existing);

        log.info("Downgrade scheduled for user: {} to {} at {}", existing.getUserGuid(), targetStatus, existing.getExpiresAt());

        return subscriptionMapper.toResponse(existing, currentStatus);
    }

    @Transactional(readOnly = true)
    public SubscriptionResponse get() {
        UUID userGuid = permissionHelper.getToken().getGuid();

        Status currentStatus = userRepository.findByGuid(userGuid)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_USER, userGuid)))
                .getStatus();

        UserSubscription subscription = userSubscriptionRepository.findByUserGuid(userGuid)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_SUBSCRIPTION, userGuid)));

        return subscriptionMapper.toResponse(subscription, currentStatus);
    }

    @Transactional
    public SubscriptionResponse updateAutoRenew(Boolean enable) {
        UUID userGuid = permissionHelper.getToken().getGuid();

        UserSubscription subscription = userSubscriptionRepository.findByUserGuid(userGuid)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_SUBSCRIPTION, userGuid)));

        subscription.setAutoRenew(enable);
        userSubscriptionRepository.save(subscription);

        Status currentStatus = userRepository.findByGuid(userGuid)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_USER, userGuid)))
                .getStatus();

        return subscriptionMapper.toResponse(subscription, currentStatus);
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

    @Transactional
    public void handleExpiry(UserSubscription subscription, Instant now) {
        UUID userGuid = subscription.getUserGuid();

        if (!subscription.isAutoRenew()) {
            resetToDefault(userGuid);
            return;
        }

        Status currentStatus = userRepository.findByGuid(userGuid)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_USER, userGuid)))
                .getStatus();

        SubscriptionPlan plan = subscriptionPlanRepository.findByStatus(currentStatus)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_SUBSCRIPTION_PLAN, currentStatus)));

        if (plan.getTier() == 0) {
            return;
        }

        chargeAndRenew(subscription, currentStatus, plan.getPrice(), now);
    }

    @Transactional
    public void handleScheduledChange(UserSubscription subscription, Instant now) {
        UUID userGuid = subscription.getUserGuid();
        Status targetStatus = subscription.getNewStatus();

        SubscriptionPlan targetPlan = subscriptionPlanRepository.findByStatus(targetStatus)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_SUBSCRIPTION_PLAN, targetStatus)));

        if (targetPlan.getTier() == 0) {
            resetToDefault(userGuid);
            return;
        }

        chargeAndRenew(subscription, targetStatus, targetPlan.getPrice(), now);
    }

    private void chargeAndRenew(UserSubscription subscription, Status targetStatus, BigDecimal price, Instant now) {
        UUID userGuid = subscription.getUserGuid();

        SubscriptionTransactionRequest request = SubscriptionTransactionRequest.newBuilder()
                .setUserGuid(userGuid.toString())
                .setAmount(price.toPlainString())
                .setType("SUBSCRIPTION_RENEWAL")
                .build();

        try {
            grpcBankClient.charge(request);

            subscription.setStartedAt(now);
            subscription.setExpiresAt(now.plus(SubscriptionHelper.SUBSCRIPTION_PERIOD_DAYS, ChronoUnit.DAYS));
            subscription.setNewStatus(null);
            subscription.setStatusChangeAt(null);
            userSubscriptionRepository.save(subscription);
            userRepository.updateStatus(userGuid, targetStatus.name());
        } catch (BadRequestException e) {
            log.warn("Insufficient funds, dropping to DEFAULT: userGuid={}", userGuid);
            resetToDefault(userGuid);
        } catch (Exception e) {
            log.error("Bank unavailable, skipping: userGuid={}", userGuid, e);
        }
    }

    private void resetToDefault(UUID userGuid) {
        userRepository.updateStatus(userGuid, Status.DEFAULT.name());
    }

    @Transactional(readOnly = true)
    public Page<UserSubscription> findExpiringOrScheduled(Instant now, PageRequest pageRequest) {
        return userSubscriptionRepository.findExpiringOrScheduled(now, pageRequest);
    }
}
