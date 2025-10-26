package com.websocket_hub.serializer;

import com.websocket_hub.domain.dto.Message;

public interface MessageSerializer<T> {

    T serialize(Message<T> message) throws Exception;
}
