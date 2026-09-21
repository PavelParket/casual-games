package com.casualgames.cronstarter.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CronJobDescriptor {

    String code;

    String description;
}
