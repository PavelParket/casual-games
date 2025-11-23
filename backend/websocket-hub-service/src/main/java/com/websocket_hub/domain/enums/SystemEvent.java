package com.websocket_hub.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SystemEvent implements EventType {

    JOIN("joined"),
    LEAVE("left");

    private final String description;

    public SystemEvent fromDescription(String description) {
        return EventType.fromDescription(description, SystemEvent.class);
    }
}
