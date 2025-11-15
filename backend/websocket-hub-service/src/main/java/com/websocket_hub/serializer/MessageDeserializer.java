package com.websocket_hub.serializer;

public interface MessageDeserializer {

    <T> T deserialize(String message, Class<T> clazz);
}
