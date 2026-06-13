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
public class DurakTurnTimerScheduler {

    public static final int TURN_SECONDS = 30;

    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(4);

    private final ConcurrentHashMap<UUID, ScheduledFuture<Void>> activeTimers = new ConcurrentHashMap<>();

    public void startTimer(UUID roomId, Runnable onTimeout) {
        cancelTimer(roomId);

        @SuppressWarnings("unchecked")
        ScheduledFuture<Void> scheduledFuture = (ScheduledFuture<Void>) scheduledExecutorService.schedule(() -> {
                    activeTimers.remove(roomId);

                    try {
                        onTimeout.run();
                    } catch (Exception e) {
                        log.error("Error in turn-timer callback for room={}", roomId, e);
                    }
                },
                TURN_SECONDS,
                TimeUnit.SECONDS
        );

        activeTimers.put(roomId, scheduledFuture);

        log.info("Turn timer started for room={} ({}s)", roomId, TURN_SECONDS);
    }

    public void resetTimer(UUID roomId, Runnable onTimeout) {
        startTimer(roomId, onTimeout);
    }

    public void cancelTimer(UUID roomId) {
        ScheduledFuture<Void> future = activeTimers.remove(roomId);

        if (future != null && !future.isDone()) {
            future.cancel(false);
        }
    }

    public boolean isActive(UUID roomId) {
        ScheduledFuture<Void> future = activeTimers.get(roomId);
        return future != null && !future.isDone() && !future.isCancelled();
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down DurakTurnTimerScheduler");
        scheduledExecutorService.shutdownNow();
    }
}
