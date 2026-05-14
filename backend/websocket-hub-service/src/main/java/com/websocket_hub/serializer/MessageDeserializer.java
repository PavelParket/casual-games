package com.websocket_hub.serializer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageDeserializer implements Deserializer<String> {

    private final ObjectMapper objectMapper;

    @Override
    public <T> T deserialize(String message, Class<T> clazz) {
        try {
            return objectMapper.readValue(message, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize message", e);
        }
    }

    public String deserializeEvent(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            JsonNode eventNode = node.get("event");

            return eventNode != null ? eventNode.asText() : null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize event from message", e);
        }
    }
}
