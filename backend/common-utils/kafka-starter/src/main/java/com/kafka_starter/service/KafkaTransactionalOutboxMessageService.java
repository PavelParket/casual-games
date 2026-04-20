package com.kafka_starter.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafka_starter.entity.KafkaOutboxMessage;
import com.kafka_starter.repository.KafkaOutboxMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaTransactionalOutboxMessageService {

    private final KafkaOutboxMessageRepository kafkaOutboxMessageRepository;

    private final ObjectMapper objectMapper;

    public void save(String topic, Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);

            KafkaOutboxMessage kafkaOutboxMessage = KafkaOutboxMessage.builder()
                    .id(UUID.randomUUID())
                    .topic(topic)
                    .messageId(UUID.randomUUID())
                    .messagePayload(json)
                    .sent(false)
                    .createdDate(Instant.now())
                    .build();

            kafkaOutboxMessageRepository.save(kafkaOutboxMessage);

            log.debug("Outbox event saved: topic={}, messageId={}", topic, kafkaOutboxMessage.getMessageId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize outbox payload: topic={}", topic, e);
            throw new IllegalArgumentException("Failed to serialize outbox event payload", e);
        }
    }
}
