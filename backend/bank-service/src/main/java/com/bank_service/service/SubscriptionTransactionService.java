package com.bank_service.service;

import com.bank_service.domain.entity.Transaction;
import com.bank_service.domain.enums.TransactionStatus;
import com.bank_service.domain.enums.TransactionType;
import com.bank_service.repository.TransactionRepository;
import com.kafka_starter.dto.event.UpdateSubscriptionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionTransactionService {

    private final TransactionRepository transactionRepository;

    @Transactional
    public void processUpdateSubscription(UpdateSubscriptionEvent event) {
        if (event == null) {
            return;
        }

        Transaction transaction = Transaction.builder()
                .userGuid(event.getUserGuid())
                .type(TransactionType.valueOf(event.getType()))
                .status(TransactionStatus.SUCCESS)
                .amount(event.getAmount())
                .balanceBefore(event.getBalanceBefore())
                .balanceAfter(event.getBalanceAfter())
                .createdAt(event.getTimestamp())
                .build();

        transactionRepository.save(transaction);
    }
}
