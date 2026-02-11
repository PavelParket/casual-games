package com.websocket_hub.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageDeserializer implements Deserializer<String> {

    private final ObjectMapper mapper;

    @Override
    public <T> T deserialize(String message, Class<T> clazz) {
        try {
            return mapper.readValue(message, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize message", e);
        }
    }
}
