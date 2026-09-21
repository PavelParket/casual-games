package com.casualgames.websockethub.factory;

public interface ObjectFactory<T> {

    T create(Object... objects);
}
