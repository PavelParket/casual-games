package casualgames.userservice.service.scheduler;

import casualgames.userservice.service.UserSubscriptionService;
import com.cron_starter.service.CronService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionScheduler implements CronService {

    private static final String CODE = "subscription-processing";
    private static final String DESCRIPTION = "Process expiring and scheduled user subscriptions";

    private final UserSubscriptionService userSubscriptionService;

    @Scheduled(cron = "${cron.update-user-subscriptions}", zone = "UTC")
    @SchedulerLock(lockAtLeastFor = "PT5M", lockAtMostFor = "PT30M", name = CODE)
    public void scheduled() {
        run();
    }

    @Override
    public void run() {
        log.info("Started job: {} at {}", DESCRIPTION, Instant.now());

        userSubscriptionService.processExpiringSubscriptions();

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
