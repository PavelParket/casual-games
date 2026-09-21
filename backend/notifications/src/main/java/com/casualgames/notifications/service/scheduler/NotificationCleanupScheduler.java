package com.casualgames.notifications.service.scheduler;

import com.casualgames.cronstarter.service.CronService;
import com.casualgames.notifications.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationCleanupScheduler implements CronService {

    private static final String CODE = "notification-cleanup";
    private static final String DESCRIPTION = "Notifications cleanup";

    private final NotificationService notificationService;

    @Scheduled(cron = "${cron.notification-cleanup}", zone = "UTC")
    @SchedulerLock(lockAtLeastFor = "PT5M", lockAtMostFor = "PT30M", name = CODE)
    public void scheduled() {
        run();
    }

    @Override
    public void run() {
        Instant now = Instant.now();

        log.info("Started job: {} at {}", DESCRIPTION, now);

        notificationService.deleteReadNotifications(now);

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
