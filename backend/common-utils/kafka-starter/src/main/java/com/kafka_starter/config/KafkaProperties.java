package com.kafka_starter.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ConfigurationProperties(prefix = "kafka")
public class KafkaProperties {

    boolean enabled;

    boolean consumerEnabled;

    String bootstrapServers;

    @Builder.Default
    Map<String, Object> consumerConfig = new HashMap<>();

    @Builder.Default
    Map<String, Object> producerConfig = new HashMap<>();

    @Builder.Default
    KafkaTopics topics = new KafkaTopics();
}
