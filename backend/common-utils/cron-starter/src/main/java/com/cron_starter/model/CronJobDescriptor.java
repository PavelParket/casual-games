package com.cron_starter.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CronJobDescriptor {

    String code;

    String description;
}
