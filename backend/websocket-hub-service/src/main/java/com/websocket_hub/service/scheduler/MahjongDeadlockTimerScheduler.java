package com.websocket_hub.service.scheduler;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class MahjongDeadlockTimerScheduler {

    public static final int DRAW_WINDOW_SECONDS = 15;

    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(4);

    private final ConcurrentHashMap<UUID, ScheduledFuture<Void>> activeTimers = new ConcurrentHashMap<>();

    public void startIfAbsent(UUID roomId, Runnable onExpired) {
        activeTimers.computeIfAbsent(roomId, key -> {
            @SuppressWarnings("unchecked")
            ScheduledFuture<Void> scheduledFuture = (ScheduledFuture<Void>) scheduledExecutorService.schedule(() -> {
                        activeTimers.remove(roomId);

                        try {
                            onExpired.run();
                        } catch (Exception e) {
                            log.error("Error in deadlock-timer callback for room={}", roomId, e);
                        }
                    },
                    DRAW_WINDOW_SECONDS,
                    TimeUnit.SECONDS
            );

            log.info("Deadlock timer started for room={} ({}s)", roomId, DRAW_WINDOW_SECONDS);

            return scheduledFuture;
        });
    }

    public void cancel(UUID roomId) {
        ScheduledFuture<Void> future = activeTimers.remove(roomId);

        if (future != null && !future.isDone()) {
            future.cancel(false);
        }
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down MahjongDeadlockTimerScheduler");
        scheduledExecutorService.shutdownNow();
    }
}
