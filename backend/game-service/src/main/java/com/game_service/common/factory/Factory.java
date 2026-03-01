package com.game_service.common.factory;

public interface Factory<T> {

    T create(Object... args);
}
