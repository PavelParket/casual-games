package com.casualgames.kafkastarter.service;

import com.casualgames.kafkastarter.entity.KafkaOutboxMessage;
import com.casualgames.kafkastarter.repository.KafkaOutboxMessageRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaTransactionalOutboxMessageService {

    private final KafkaOutboxMessageRepository kafkaOutboxMessageRepository;

    @Qualifier("kafkaObjectMapper")
    private final ObjectMapper objectMapper;

    public void save(String topic, Object payload) {
        save(topic, null, payload);
    }

    public void save(String topic, String partitionKey, Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);

            KafkaOutboxMessage kafkaOutboxMessage = KafkaOutboxMessage.builder()
                    .id(UUID.randomUUID())
                    .topic(topic)
                    .messageId(UUID.randomUUID())
                    .partitionKey(partitionKey)
                    .messagePayload(json)
                    .sent(false)
                    .createdDate(Instant.now())
                    .build();

            kafkaOutboxMessageRepository.save(kafkaOutboxMessage);

            log.debug("Outbox event saved: topic={}, messageId={}, partitionKey={}", topic, kafkaOutboxMessage.getMessageId(), partitionKey);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize outbox payload: topic={}", topic, e);
            throw new IllegalArgumentException("Failed to serialize outbox event payload", e);
        }
    }
}
