package casualgames.userservice.service.scheduler;

import casualgames.userservice.domain.entity.UserSubscription;
import casualgames.userservice.service.UserSubscriptionService;
import com.cron_starter.service.CronService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionExpiringInDaysScheduler implements CronService {

    private static final String CODE = "subscription-expiring-notification";
    private static final String DESCRIPTION = "Notify users about subscriptions expiring";

    private static final int INITIAL_PAGE_SIZE = 0;
    private static final int PAGE_SIZE = 100;

    private final UserSubscriptionService userSubscriptionService;

    @Scheduled(cron = "${cron.notify-expiring-subscriptions}", zone = "UTC")
    @SchedulerLock(lockAtLeastFor = "PT5M", lockAtMostFor = "PT30M", name = CODE)
    public void scheduled() {
        run();
    }

    @Override
    public void run() {
        Instant now = Instant.now();

        log.info("Started job: {} at {}", DESCRIPTION, now);

        int page = INITIAL_PAGE_SIZE;
        Page<UserSubscription> subscriptionsPage;

        do {
            subscriptionsPage = userSubscriptionService.findExpiringInDays(now, PageRequest.of(page, PAGE_SIZE));

            subscriptionsPage.getContent()
                    .forEach(subscription -> userSubscriptionService.sendExpiringInDaysNotification(subscription, now));

            page++;
        } while (subscriptionsPage.hasNext());

        log.info("Finished job: {} at {}", DESCRIPTION, Instant.now());
    }

    @Override
    public String getCode() {
        return CODE;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }
}
