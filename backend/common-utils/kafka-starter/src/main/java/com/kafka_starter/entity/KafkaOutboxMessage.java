package com.kafka_starter.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactional_outbox_kafka_messages")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KafkaOutboxMessage {

    @Id
    private UUID id;

    private String topic;

    @Column(updatable = false)
    private UUID messageId;

    @Column(updatable = false)
    private String partitionKey;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String messagePayload;

    private boolean sent;

    @Column(updatable = false)
    private Instant createdDate;
}
