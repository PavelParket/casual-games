package com.kafka_starter.config;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@ConditionalOnProperty(prefix = "kafka.transactional-outbox", name = "enabled", havingValue = "true")
@ConditionalOnClass(SpringLiquibase.class)
@EnableConfigurationProperties(KafkaTransactionalOutboxProperties.class)
@Import(KafkaStarterLiquibaseConfig.class)
public class KafkaTransactionalOutboxConfiguration {
}
