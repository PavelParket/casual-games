package com.websocket_hub.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.websocket_hub.domain.dto.message.Message;
import com.websocket_hub.domain.enums.events.EventType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageSerializer implements Serializer<Message<? extends EventType>, String> {

    private final ObjectMapper objectMapper;

    @Override
    public String serialize(Message<? extends EventType> message) throws Exception {
        return objectMapper.writeValueAsString(message);
    }
}
