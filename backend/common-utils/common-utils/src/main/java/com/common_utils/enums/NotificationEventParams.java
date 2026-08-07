package com.common_utils.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationEventParams {

    USERNAME("username"),
    TIER("tier"),
    DAYS_LEFT("daysLeft"),
    ROOM_HANDLER("roomHandler"),
    ROOM_NAME("roomName"),
    ROOM_ID("roomId");

    private final String param;
}
