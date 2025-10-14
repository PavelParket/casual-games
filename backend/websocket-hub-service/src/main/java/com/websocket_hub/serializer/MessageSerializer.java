package com.websocket_hub.serializer;

import com.websocket_hub.dto.Message;

public interface MessageSerializer<T> {

    T serialize(Message<T> message) throws Exception;
}
