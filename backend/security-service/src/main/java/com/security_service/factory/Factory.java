package com.security_service.factory;

public interface Factory<T> {

    T create(Object... args);
}
