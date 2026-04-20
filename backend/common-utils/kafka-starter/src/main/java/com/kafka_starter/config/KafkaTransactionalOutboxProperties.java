package com.kafka_starter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "kafka.transactional-outbox")
public class KafkaTransactionalOutboxProperties {

    private boolean enabled = false;

    private Integer deleteEventsAfterDays = 7;

    private String cleanerCron = "0 0 3 * * *";
}
