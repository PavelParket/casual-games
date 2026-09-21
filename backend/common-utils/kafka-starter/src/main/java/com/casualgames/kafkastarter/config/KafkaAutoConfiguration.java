package com.casualgames.kafkastarter.config;

import com.casualgames.kafkastarter.service.KafkaMessageService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnProperty(prefix = "kafka", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(KafkaProperties.class)
@Import({
        KafkaProducerConfig.class,
        KafkaConsumerConfig.class,
        KafkaMessageService.class,
        KafkaTransactionalOutboxConfiguration.class,
        KafkaJacksonConfig.class
})
public class KafkaAutoConfiguration {

    @Bean
    public KafkaTopics kafkaTopics(KafkaProperties kafkaProperties) {
        return kafkaProperties.getTopics();
    }
}
