package com.bank_service.service.listener;

import com.bank_service.service.SubscriptionTransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafka_starter.dto.event.UpdateSubscriptionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UpdateSubscriptionEventListener {

    private final ObjectMapper objectMapper;

    private final SubscriptionTransactionService subscriptionTransactionService;

    @KafkaListener(topics = "#{kafkaTopics.updateSubscription}", groupId = "${kafka.consumer-config.[group.id]}")
    public void handleUpdateSubscription(String message) {
        try {
            UpdateSubscriptionEvent event = objectMapper.readValue(message, UpdateSubscriptionEvent.class);

            log.info("Processing update subscription event: userGuid={}, type={}", event.getUserGuid(), event.getType());

            subscriptionTransactionService.processUpdateSubscription(event);
        } catch (Exception e) {
            log.error("Failed to process update subscription event: message={}", message, e);
            throw new RuntimeException("Subscription update event processing failed", e);
        }
    }
}
