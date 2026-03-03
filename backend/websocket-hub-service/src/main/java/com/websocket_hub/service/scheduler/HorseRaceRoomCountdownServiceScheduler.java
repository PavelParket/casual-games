package com.websocket_hub.service.scheduler;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class HorseRaceRoomCountdownServiceScheduler {

    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(4);

    private final ConcurrentHashMap<UUID, ScheduledFuture<?>> activeTimers = new ConcurrentHashMap<>();

    public void startCountdown(UUID roomId, long delaySeconds, Runnable onFinish) {
        cancelCountdown(roomId);

        ScheduledFuture<?> future = scheduledExecutorService.schedule(() -> {
            activeTimers.remove(roomId);

            try {
                onFinish.run();
            } catch (Exception e) {
                log.error("Error in countdown callback for room={}", roomId, e);
            }
        }, delaySeconds, TimeUnit.SECONDS);

        activeTimers.put(roomId, future);

        log.info("Countdown started for room={}, delay={}s", roomId, delaySeconds);
    }

    public void cancelCountdown(UUID roomId) {
        ScheduledFuture<?> future = activeTimers.remove(roomId);

        if (future != null && !future.isDone()) {
            future.cancel(false);
            log.info("Countdown cancelled for room={}", roomId);
        }
    }

    public boolean isActive(UUID roomId) {
        ScheduledFuture<?> future = activeTimers.get(roomId);

        return future != null && !future.isDone() && !future.isCancelled();
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down RoomCountdownService scheduler");
        scheduledExecutorService.shutdownNow();
    }
}
