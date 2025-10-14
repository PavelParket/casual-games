package com.websocket_hub.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.websocket_hub.dto.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JsonSerializer implements MessageSerializer<String> {

    private final ObjectMapper mapper;

    @Override
    public String serialize(Message<String> message) throws Exception {
        return mapper.writeValueAsString(message);
    }
}
