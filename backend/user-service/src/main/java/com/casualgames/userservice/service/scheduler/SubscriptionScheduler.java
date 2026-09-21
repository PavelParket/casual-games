package com.casualgames.userservice.service.scheduler;

import com.casualgames.cronstarter.service.CronService;
import com.casualgames.userservice.domain.entity.UserSubscription;
import com.casualgames.userservice.service.UserSubscriptionService;
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
public class SubscriptionScheduler implements CronService {

    private static final String CODE = "subscription-processing";
    private static final String DESCRIPTION = "Process expiring and scheduled user subscriptions";

    private static final int INITIAL_PAGE_SIZE = 0;
    private static final int PAGE_SIZE = 100;

    private final UserSubscriptionService userSubscriptionService;

    @Scheduled(cron = "${cron.update-user-subscriptions}", zone = "UTC")
    @SchedulerLock(lockAtLeastFor = "PT5M", lockAtMostFor = "PT30M", name = CODE)
    public void scheduled() {
        run();
    }

    // TODO: reanalyze and refactor process
    @Override
    public void run() {
        log.info("Started job: {} at {}", DESCRIPTION, Instant.now());

        Instant now = Instant.now();
        int page = INITIAL_PAGE_SIZE;
        Page<UserSubscription> subscriptionsPage;

        do {
            subscriptionsPage = userSubscriptionService.findExpiringOrScheduled(now, PageRequest.of(page, PAGE_SIZE));

            subscriptionsPage.getContent().forEach(subscription -> userSubscriptionService.processOne(subscription, now));

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
