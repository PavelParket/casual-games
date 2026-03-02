package com.websocket_hub.serializer;

public interface Deserializer<S> {

    <T> T deserialize(S source, Class<T> clazz);
}
