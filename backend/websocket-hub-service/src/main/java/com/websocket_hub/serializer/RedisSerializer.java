package com.websocket_hub.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisSerializer implements Serializer<Object, String> {

    private final ObjectMapper objectMapper;

    @Override
    public String serialize(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }
}
