package com.websocket_hub.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MessageType {

    SYSTEM("system");

    private final String type;
}
