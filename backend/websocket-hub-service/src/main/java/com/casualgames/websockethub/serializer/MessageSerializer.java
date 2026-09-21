package com.casualgames.websockethub.serializer;

import com.casualgames.websockethub.domain.dto.message.Message;
import com.casualgames.websockethub.domain.enums.events.EventType;
import com.fasterxml.jackson.databind.ObjectMapper;
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
