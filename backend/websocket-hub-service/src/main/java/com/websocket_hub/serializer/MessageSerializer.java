package com.websocket_hub.serializer;

import com.websocket_hub.domain.dto.message.Message;

public interface MessageSerializer<T> {

    T serialize(Message message) throws Exception;
}
