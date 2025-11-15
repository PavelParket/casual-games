package com.websocket_hub.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SystemEvent implements EventType {

    JOIN("joined"),
    LEAVE("left");

    private final String description;
}
