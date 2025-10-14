package com.websocket_hub.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SystemEvents {

    JOIN("joined"),
    LEFT("left");

    private final String description;
}
