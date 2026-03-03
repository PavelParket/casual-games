package com.websocket_hub.config.properies;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "scheduler.room-cleanup")
public record RoomCleanupProperties(

        long schedulerDelayMinutes,

        long schedulerInitialDelayMinutes,

        long finishedKickTimeoutMinutes,

        long pendingDeleteDelaySeconds

) {
}
