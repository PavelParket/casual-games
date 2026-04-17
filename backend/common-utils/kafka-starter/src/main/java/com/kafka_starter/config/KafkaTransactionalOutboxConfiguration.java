package com.kafka_starter.config;

import com.kafka_starter.service.KafkaTransactionalOutboxMessageScheduler;
import com.kafka_starter.service.KafkaTransactionalOutboxMessageService;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@ConditionalOnProperty(prefix = "kafka.transactional-outbox", name = "enabled", havingValue = "true")
@ConditionalOnClass(SpringLiquibase.class)
@EnableJpaRepositories(basePackages = "com.kafka_starter")
@AutoConfigurationPackage(basePackages = "com.kafka_starter")
@EnableConfigurationProperties(KafkaTransactionalOutboxProperties.class)
@Import({
        KafkaStarterLiquibaseConfig.class,
        KafkaTransactionalOutboxMessageService.class,
        KafkaTransactionalOutboxMessageScheduler.class
})
public class KafkaTransactionalOutboxConfiguration {
}
