package com.casualgames.websockethub.serializer;

public interface Deserializer<S> {

    <T> T deserialize(S source, Class<T> clazz);
}
