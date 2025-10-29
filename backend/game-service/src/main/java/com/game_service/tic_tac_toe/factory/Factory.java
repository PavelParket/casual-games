package com.game_service.tic_tac_toe.factory;

public interface Factory<T> {

    T create(Object... args);
}
