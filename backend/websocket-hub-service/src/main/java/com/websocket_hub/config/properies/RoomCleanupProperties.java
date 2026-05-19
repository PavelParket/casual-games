package com.websocket_hub.config.properies;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cron.room-cleanup")
public record RoomCleanupProperties(

        String cleanupRooms,

        String cleanupPendingDeleteRooms,

        long finishedKickTimeoutMinutes,

        long waitingStaleTimeoutMinutes,

        long ticTacToeInProgressTimeoutMinutes,

        long durakInProgressTimeoutMinutes,

        long deCoderInProgressTimeoutMinutes
) {
}
