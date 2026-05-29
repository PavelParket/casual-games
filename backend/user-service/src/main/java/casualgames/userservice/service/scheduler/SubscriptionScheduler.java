package casualgames.userservice.service.scheduler;

import casualgames.userservice.domain.entity.UserSubscription;
import casualgames.userservice.service.UserSubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionScheduler {

    private static final int INITIAL_PAGE_SIZE = 0;
    private static final int PAGE_SIZE = 100;

    private final UserSubscriptionService userSubscriptionService;

    @Scheduled(cron = "${cron.update-user-subscriptions}", zone = "UTC")
    public void run() {
        Instant now = Instant.now();

        log.info("Subscription scheduler started: now={}", now);

        int page = INITIAL_PAGE_SIZE;
        Page<UserSubscription> subscriptionsPage;

        do {
            subscriptionsPage = userSubscriptionService.findExpiringOrScheduled(now, PageRequest.of(page, PAGE_SIZE));

            subscriptionsPage.getContent().forEach(subscription -> userSubscriptionService.processOne(subscription, now));

            page++;
        } while (subscriptionsPage.hasNext());

        log.info("Subscription processing completed");
    }
}
