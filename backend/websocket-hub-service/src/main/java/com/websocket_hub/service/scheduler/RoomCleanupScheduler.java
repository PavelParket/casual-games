package com.websocket_hub.service.scheduler;

import com.websocket_hub.config.properies.RoomCleanupProperties;
import com.websocket_hub.domain.entity.RoomMetadata;
import com.websocket_hub.domain.enums.RoomStatus;
import com.websocket_hub.manager.AbstractRoomManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomCleanupScheduler {

    private final RoomCleanupProperties roomCleanupProperties;

    private final List<AbstractRoomManager> roomManagers;

    @Scheduled(fixedDelayString = "${scheduler.room-cleanup.scheduler-delay-minutes}",
            initialDelayString = "${scheduler.room-cleanup.scheduler-initial-delay-minutes}",
            timeUnit = TimeUnit.MINUTES)
    public void cleanup() {
        log.info("Room cleanup: first iteration started");

        runMainCleanup();

        log.info("Room cleanup: first iteration finished, scheduling second iteration in {}m", roomCleanupProperties.pendingDeleteDelaySeconds());

        Thread.ofVirtual().start(() -> {
            try {
                Thread.sleep(Duration.ofMinutes(roomCleanupProperties.pendingDeleteDelaySeconds()));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Room cleanup: second iteration interrupted before start");
                return;
            }

            log.info("Room cleanup: second iteration started");
            runPendingDeleteCleanup();
            log.info("Room cleanup: second iteration finished");
        });
    }

    private void runMainCleanup() {
        roomManagers.stream()
                .filter(manager -> manager.getRedisKey() != null)
                .forEach(manager -> {
                    try {
                        manager.getAllMetadata().forEach(meta -> processMainCleanup(meta, manager));
                    } catch (Exception e) {
                        log.error("Room cleanup main iteration failed for manager={}", manager.getRoomType(), e);
                    }
                });
    }

    private void processMainCleanup(RoomMetadata metadata, AbstractRoomManager roomManager) {
        RoomStatus status = metadata.getStatus() != null ? metadata.getStatus() : RoomStatus.WAITING;

        switch (status) {
            case WAITING -> {
                if (metadata.getParticipantCount() == 0) {
                    log.info("Room cleanup: marking PENDING_DELETE — roomId={}, type={}", metadata.getId(), metadata.getType());
                    roomManager.updateRoomStatus(metadata.getId(), RoomStatus.PENDING_DELETE);
                }
            }

            case FINISHED -> {
                boolean timeoutOver = metadata.getGameFinishedAt() != null
                        && Instant.now().isAfter(metadata.getGameFinishedAt().plus(Duration.ofMinutes(roomCleanupProperties.finishedKickTimeoutMinutes())));

                if (timeoutOver) {
                    log.info("Room cleanup: kicking and deleting FINISHED room — roomId={}, type={}", metadata.getId(), metadata.getType());
                    roomManager.kickAll(metadata.getId());
                    roomManager.delete(metadata.getId());
                }
            }
        }
    }

    private void runPendingDeleteCleanup() {
        roomManagers.stream()
                .filter(roomManager -> roomManager.getRedisKey() != null)
                .forEach(roomManager -> {
                    try {
                        roomManager.getAllMetadata().stream()
                                .filter(roomMetadata -> RoomStatus.PENDING_DELETE.equals(roomMetadata.getStatus()))
                                .forEach(roomMetadata -> processPendingDeleteCleanup(roomMetadata, roomManager));
                    } catch (Exception e) {
                        log.error("Room cleanup second iteration failed for manager={}", roomManager.getRoomType(), e);
                    }
                });
    }

    private void processPendingDeleteCleanup(RoomMetadata metadata, AbstractRoomManager roomManager) {
        if (metadata.getParticipantCount() == 0) {
            log.info("Room cleanup: deleting PENDING_DELETE room — roomId={}, type={}", metadata.getId(), metadata.getType());
            roomManager.delete(metadata.getId());
        } else {
            log.info("Room cleanup: rolling back PENDING_DELETE to WAITING (players present) — roomId={}, type={}", metadata.getId(), metadata.getType());
            roomManager.updateRoomStatus(metadata.getId(), RoomStatus.WAITING);
        }
    }
}
