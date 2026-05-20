package com.websocket_hub.service.scheduler;

import com.websocket_hub.config.properies.RoomCleanupProperties;
import com.websocket_hub.domain.entity.RoomMetadata;
import com.websocket_hub.domain.enums.RoomStatus;
import com.websocket_hub.domain.enums.RoomType;
import com.websocket_hub.manager.AbstractRoomManager;
import com.websocket_hub.service.helper.KafkaMessageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomCleanupScheduler {

    private final static String CLEANUP_EMPTY = "CLEANUP_EMPTY";
    private final static String CLEANUP_WAITING_STALE = "CLEANUP_WAITING_STALE";
    private final static String CLEANUP_IN_PROGRESS_EMPTY = "CLEANUP_IN_PROGRESS_EMPTY";
    private final static String CLEANUP_IN_PROGRESS_STALE = "CLEANUP_IN_PROGRESS_STALE";
    private final static String CLEANUP_FINISHED = "CLEANUP_FINISHED";

    private final RoomCleanupProperties roomCleanupProperties;

    private final List<AbstractRoomManager> roomManagers;

    private final KafkaMessageHelper kafkaMessageHelper;

    @Scheduled(cron = "${cron.room-cleanup.cleanup-rooms}")
    public void runCleanup() {
        log.info("Room cleanup: main pass started");

        roomManagers.stream()
                .filter(manager -> Objects.nonNull(manager.getRedisKey()))
                .forEach(manager -> {
                    try {
                        manager.getAllMetadata().forEach(meta -> processRoom(meta, manager));
                    } catch (Exception e) {
                        log.error("Room cleanup main pass failed for manager={}", manager.getRoomType(), e);
                    }
                });

        log.info("Room cleanup: main pass finished");
    }

    @Scheduled(cron = "${cron.room-cleanup.cleanup-pending-delete-rooms}")
    public void runCleanupPendingDelete() {
        log.info("Room cleanup: pending-delete pass started");

        roomManagers.stream()
                .filter(manager -> manager.getRedisKey() != null)
                .forEach(manager -> {
                    try {
                        manager.getAllMetadata().stream()
                                .filter(meta -> RoomStatus.PENDING_DELETE.equals(meta.getStatus()))
                                .forEach(meta -> processPendingDelete(meta, manager));
                    } catch (Exception e) {
                        log.error("Room cleanup pending-delete pass failed for manager={}", manager.getRoomType(), e);
                    }
                });

        log.info("Room cleanup: pending-delete pass finished");
    }

    private void processRoom(RoomMetadata metadata, AbstractRoomManager manager) {
        RoomStatus status = metadata.getStatus() != null ? metadata.getStatus() : RoomStatus.WAITING;

        switch (status) {
            case WAITING -> handleWaiting(metadata, manager);
            case IN_PROGRESS -> handleInProgress(metadata, manager);
            case FINISHED -> handleFinished(metadata, manager);
            case PENDING_DELETE -> { /* handled in pending-delete pass */ }
        }
    }

    private void handleWaiting(RoomMetadata metadata, AbstractRoomManager manager) {
        if (metadata.getParticipantCount() == 0) {
            manager.updateRoomStatus(metadata.getId(), RoomStatus.PENDING_DELETE);

        } else if (isStale(metadata.getCreatedAt(), roomCleanupProperties.waitingStaleTimeoutMinutes())) {
            kickAndDelete(metadata, manager, CLEANUP_WAITING_STALE);
        }
    }

    private void handleInProgress(RoomMetadata metadata, AbstractRoomManager manager) {
        RoomType type = metadata.getType();

        if (type == RoomType.HORSE_RACE) {
            return;
        }

        long timeoutMinutes = getInProgressTimeoutMinutes(type);
        Instant anchor = metadata.getGameStartedAt() != null
                ? metadata.getGameStartedAt()
                : metadata.getCreatedAt();

        if (metadata.getParticipantCount() == 0) {
            if (type.isAllowsLateJoin()) {
                if (isStale(anchor, timeoutMinutes)) {
                    manager.updateRoomStatus(metadata.getId(), RoomStatus.PENDING_DELETE);
                }
            } else {
                deleteRoom(metadata, manager, CLEANUP_IN_PROGRESS_EMPTY);
            }
        } else if (isStale(anchor, timeoutMinutes)) {
            kickAndDelete(metadata, manager, CLEANUP_IN_PROGRESS_STALE);
        }
    }

    private void handleFinished(RoomMetadata metadata, AbstractRoomManager manager) {
        if (isStale(metadata.getGameFinishedAt(), roomCleanupProperties.finishedKickTimeoutMinutes())) {
            kickAndDelete(metadata, manager, CLEANUP_FINISHED);
        }
    }

    private void processPendingDelete(RoomMetadata metadata, AbstractRoomManager manager) {
        if (metadata.getParticipantCount() == 0) {
            deleteRoom(metadata, manager, CLEANUP_EMPTY);
        } else {
            RoomStatus rollback = metadata.getType().isAllowsLateJoin()
                    ? RoomStatus.IN_PROGRESS
                    : RoomStatus.WAITING;
            manager.updateRoomStatus(metadata.getId(), rollback);
        }
    }

    private void kickAndDelete(RoomMetadata metadata, AbstractRoomManager manager, String reason) {
        manager.kickAll(metadata.getId());
        manager.delete(metadata.getId());
        kafkaMessageHelper.sendRoomDeletedEvent(metadata.getId(), metadata.getType(), reason);
    }

    private void deleteRoom(RoomMetadata metadata, AbstractRoomManager manager, String reason) {
        manager.delete(metadata.getId());
        kafkaMessageHelper.sendRoomDeletedEvent(metadata.getId(), metadata.getType(), reason);
    }

    private boolean isStale(Instant anchor, long timeoutMinutes) {
        return anchor != null && Instant.now().isAfter(anchor.plus(Duration.ofMinutes(timeoutMinutes)));
    }

    private long getInProgressTimeoutMinutes(RoomType type) {
        return switch (type) {
            case TIC_TAC_TOE -> roomCleanupProperties.ticTacToeInProgressTimeoutMinutes();
            case DURAK -> roomCleanupProperties.durakInProgressTimeoutMinutes();
            case DE_CODER -> roomCleanupProperties.deCoderInProgressTimeoutMinutes();
            case HORSE_RACE -> Long.MAX_VALUE;
        };
    }
}
