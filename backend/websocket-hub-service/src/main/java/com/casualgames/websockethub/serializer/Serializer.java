package com.casualgames.websockethub.serializer;

public interface Serializer<S, T> {

    T serialize(S source) throws Exception;
}
