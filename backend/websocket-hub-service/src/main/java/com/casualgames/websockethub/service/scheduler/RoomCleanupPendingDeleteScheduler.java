package com.casualgames.websockethub.service.scheduler;

import com.casualgames.cronstarter.service.CronService;
import com.casualgames.websockethub.domain.entity.RoomMetadata;
import com.casualgames.websockethub.domain.enums.RoomStatus;
import com.casualgames.websockethub.manager.AbstractRoomManager;
import com.casualgames.websockethub.service.helper.KafkaMessageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomCleanupPendingDeleteScheduler implements CronService {

    private static final String CODE = "room-cleanup-pending-delete";
    private static final String DESCRIPTION = "Cleanup pending-delete rooms";

    private static final String CLEANUP_EMPTY = "CLEANUP_EMPTY";

    private final List<AbstractRoomManager> abstractRoomManagers;

    private final KafkaMessageHelper kafkaMessageHelper;

    @Scheduled(cron = "${cron.room-cleanup.cleanup-pending-delete-rooms}")
    @SchedulerLock(lockAtLeastFor = "PT3M", lockAtMostFor = "PT20M", name = CODE)
    public void scheduled() {
        run();
    }

    // TODO: reanalyze and refactor process
    @Override
    public void run() {
        log.info("Started job: {} at {}", DESCRIPTION, Instant.now());

        abstractRoomManagers.stream()
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

        log.info("Finished job: {} at {}", DESCRIPTION, Instant.now());
    }

    private void processPendingDelete(RoomMetadata metadata, AbstractRoomManager manager) {
        if (metadata.getParticipantCount() == 0) {
            deleteRoom(metadata, manager);
        } else {
            RoomStatus rollback = metadata.getType().isAllowsLateJoin()
                    ? RoomStatus.IN_PROGRESS
                    : RoomStatus.WAITING;
            manager.updateRoomStatus(metadata.getId(), rollback);
        }
    }

    private void deleteRoom(RoomMetadata metadata, AbstractRoomManager manager) {
        manager.delete(metadata.getId());
        kafkaMessageHelper.sendRoomDeletedEvent(metadata.getId(), metadata.getType(), CLEANUP_EMPTY);
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
