package com.websocket_hub.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisDeserializer implements Deserializer<String> {

    private final ObjectMapper objectMapper;

    @Override
    public <T> T deserialize(String data, Class<T> clazz) {
        try {
            return objectMapper.readValue(data, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize data", e);
        }
    }
}
