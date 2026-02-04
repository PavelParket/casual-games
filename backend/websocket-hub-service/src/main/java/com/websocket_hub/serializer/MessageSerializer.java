package com.websocket_hub.serializer;

import com.websocket_hub.domain.dto.message.Message;
import com.websocket_hub.domain.enums.EventType;

public interface MessageSerializer<T> {

    T serialize(Message<? extends EventType> message) throws Exception;
}
