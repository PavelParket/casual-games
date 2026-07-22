package com.cron_starter.service;

public interface CronService {

    String getCode();

    default String getDescription() {
        return getCode();
    }

    void run();
}
