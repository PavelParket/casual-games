package com.websocket_hub.serializer;

public interface Serializer<S, T> {

    T serialize(S source) throws Exception;
}
