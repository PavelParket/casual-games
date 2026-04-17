package com.kafka_starter.config;

import liquibase.integration.spring.SpringLiquibase;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class KafkaStarterLiquibaseConfig {

    private static final String CHANGELOG_PATH = "classpath:db/kafka-starter/db.changelog-master.yaml";
    private static final String CHANGELOG_TABLE = "kafka_starter_databasechangelog";
    private static final String CHANGELOG_LOCK_TABLE = "kafka_starter_databasechangeloglock";

    private final DataSource dataSource;

    @Bean(name = "kafkaStarterLiquibase")
    public SpringLiquibase kafkaStarterLiquibase() {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(CHANGELOG_PATH);
        liquibase.setDatabaseChangeLogTable(CHANGELOG_TABLE);
        liquibase.setDatabaseChangeLogLockTable(CHANGELOG_LOCK_TABLE);
        return liquibase;
    }
}
