package com.game_service.de_coder.factory;

public interface Factory<T> {

    T create(Object... args);
}
