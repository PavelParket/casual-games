package com.websocket_hub.domain.enums;

import java.util.Arrays;

public interface EventType {

    String getDescription();

    static <T extends Enum<T>> T fromDescription(String description, Class<T> enumType) {
        return Arrays.stream(enumType.getEnumConstants())
                .filter(event -> event.name().equalsIgnoreCase(description))
                .findFirst()
                .orElse(null);
    }
}
