package com.kafka_starter.service;

import com.cron_starter.service.CronService;
import com.kafka_starter.config.KafkaTransactionalOutboxProperties;
import com.kafka_starter.entity.KafkaOutboxMessage;
import com.kafka_starter.repository.KafkaOutboxMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaTransactionalOutboxMessageScheduler implements CronService {

    private static final String CODE = "delete-old-transactional-outbox-events";
    private static final String DESCRIPTION = "Delete old transactional outbox events";

    private final KafkaOutboxMessageRepository kafkaOutboxMessageRepository;

    private final KafkaMessageService kafkaMessageService;

    private final KafkaTransactionalOutboxProperties kafkaTransactionalOutboxProperties;

    // TODO: reanalyze and refactor process
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
    @SchedulerLock(lockAtLeastFor = "PT5M", lockAtMostFor = "PT10M", name = CODE)
    @Transactional
    public void scheduled() {
        run();
    }

    @Override
    @Transactional
    public void run() {
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

    @Override
    public String getCode() {
        return CODE;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }
}
