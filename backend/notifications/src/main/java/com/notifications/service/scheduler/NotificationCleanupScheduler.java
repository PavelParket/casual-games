package com.notifications.service.scheduler;

import com.notifications.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationCleanupScheduler {

    private final NotificationService notificationService;

    @Scheduled(cron = "${cron.notification-cleanup}", zone = "UTC")
    public void run() {
        Instant now = Instant.now();

        log.info("Notification cleanup scheduler started at {}", now);

        notificationService.deleteReadNotifications(now);

        log.info("Notification cleanup scheduler completed at {}", Instant.now());
    }
}
