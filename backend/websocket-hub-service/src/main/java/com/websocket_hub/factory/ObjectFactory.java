package com.websocket_hub.factory;

public interface ObjectFactory<T> {

    T create(Object... objects);
}
