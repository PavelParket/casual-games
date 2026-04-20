package com.kafka_starter.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaMessageService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper;

    public void send(String topic, String key, Object payload) {
        try {
            String json;

            if (payload instanceof String s) {
                json = s;
            } else {
                json = objectMapper.writeValueAsString(payload);
            }

            kafkaTemplate.send(topic, key, json)
                    .whenComplete((result, exception) -> {
                        if (exception != null) {
                            log.error("Failed to send Kafka message: topic={}, key={}", topic, key, exception);
                        } else {
                            log.debug("Kafka message sent: topic={}, key={}, offset={}", topic, key, result.getRecordMetadata().offset());
                        }
                    });
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize Kafka message: topic={}, key={}", topic, key, e);
        }
    }
}
