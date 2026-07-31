package com.common_utils.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationEventParams {

    USERNAME("username"),
    TIER("tier"),
    DAYS_LEFT("daysLeft");

    private final String param;
}
