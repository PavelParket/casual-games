package com.casualgames.cronstarter.service;

public interface CronService {

    String getCode();

    default String getDescription() {
        return getCode();
    }

    void run();
}
