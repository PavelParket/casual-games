package com.kafka_starter.config;

import com.kafka_starter.service.KafkaTransactionalOutboxMessageScheduler;
import com.kafka_starter.service.KafkaTransactionalOutboxMessageService;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@ConditionalOnProperty(prefix = "kafka.transactional-outbox", name = "enabled", havingValue = "true")
@AutoConfigurationPackage(basePackages = "com.kafka_starter")
@EnableConfigurationProperties(KafkaTransactionalOutboxProperties.class)
@Import({
        KafkaTransactionalOutboxMessageService.class,
        KafkaTransactionalOutboxMessageScheduler.class
})
public class KafkaTransactionalOutboxConfiguration {
}
