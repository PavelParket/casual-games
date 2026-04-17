package com.kafka_starter.repository;

import com.kafka_starter.entity.KafkaOutboxMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface KafkaOutboxMessageRepository extends JpaRepository<KafkaOutboxMessage, UUID> {

    List<KafkaOutboxMessage> findBySentFalseOrderByCreatedDateAsc();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            DELETE FROM transactional_outbox_kafka_messages
            WHERE sent = TRUE
            AND created_date < :date
            """, nativeQuery = true)
    int deleteSentBefore(Instant date);
}
