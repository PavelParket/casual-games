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

    @Column(nullable = false)
    private String topic;

    @Column(nullable = false, updatable = false)
    private UUID messageId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String messagePayload;

    @Column(nullable = false)
    private boolean sent;

    @Column(nullable = false, updatable = false)
    private Instant createdDate;
}
