package com.websocket_hub.serializer;

import com.websocket_hub.dto.Message;

public interface MessageSerializer {

    String serialize(Message message) throws Exception;
}
