package com.kafka_starter.service;

import com.kafka_starter.config.KafkaTransactionalOutboxProperties;
import com.kafka_starter.entity.KafkaOutboxMessage;
import com.kafka_starter.repository.KafkaOutboxMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaTransactionalOutboxMessageScheduler {

    private final KafkaOutboxMessageRepository kafkaOutboxMessageRepository;

    private final KafkaMessageService kafkaMessageService;

    private final KafkaTransactionalOutboxProperties kafkaTransactionalOutboxProperties;

    @Scheduled(fixedDelayString = "${kafka.transactional-outbox.poll-delay-ms:3000}")
    @Transactional
    public void poll() {
        try {
            List<KafkaOutboxMessage> messages = kafkaOutboxMessageRepository.findBySentFalseOrderByCreatedDateAsc();

            if (messages.isEmpty()) {
                return;
            }

            log.debug("Outbox poller: found {} unsent events", messages.size());

            messages.forEach(this::processMessage);
        } catch (Exception e) {
            log.error("Outbox poller: unexpected error during poll cycle", e);
        }
    }

    private void processMessage(KafkaOutboxMessage message) {
        try {
            kafkaMessageService.send(message.getTopic(), message.getMessageId().toString(), message.getMessagePayload());

            message.setSent(true);

            log.debug("Outbox event sent: messageId={}, topic={}", message.getMessageId(), message.getTopic());
        } catch (Exception e) {
            log.error("Outbox poller: failed to send event: messageId={}, topic={}", message.getMessageId(), message.getTopic(), e);
        }
    }

    @Scheduled(cron = "${kafka.transactional-outbox.cleaner-cron:0 0 3 * * *}")
    @Transactional
    public void cleanup() {
        try {
            Instant date = Instant.now().minus(kafkaTransactionalOutboxProperties.getDeleteEventsAfterDays(), ChronoUnit.DAYS);
            int deleted = kafkaOutboxMessageRepository.deleteSentBefore(date);

            if (deleted > 0) {
                log.info("Outbox cleanup: deleted {} processed events older than {} days", deleted, kafkaTransactionalOutboxProperties.getDeleteEventsAfterDays());
            }
        } catch (Exception e) {
            log.error("Outbox cleanup: unexpected error", e);
        }
    }
}
