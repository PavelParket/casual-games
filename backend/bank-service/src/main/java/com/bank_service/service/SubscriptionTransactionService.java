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

    /*private final SubscriptionTransactionFactory subscriptionTransactionFactory;

    private final TransactionLifecycleService transactionLifecycleService;

    private final GrpcUserTransactionClient grpcUserTransactionClient;

    @Transactional
    public Transaction processSubscription(SubscriptionTransactionRequest request) {
        log.info("Subscription charge: userGuid={}, amount={}, type={}", request.getUserGuid(), request.getAmount(), request.getType());

        BigDecimal balanceBefore = transactionRepository
                .findFirstByUserGuidAndStatusOrderByCreatedAtDesc(UUID.fromString(request.getUserGuid()), TransactionStatus.SUCCESS.name())
                .map(Transaction::getBalanceAfter)
                .orElse(BigDecimal.ZERO);

        Transaction transaction = subscriptionTransactionFactory.createSubscriptionTransaction(request, balanceBefore);

        List<Transaction> transactions = transactionLifecycleService.pending(List.of(transaction));

        try {
            grpcUserTransactionClient.sendUpdates(List.of(subscriptionTransactionFactory.createSubtractionTransaction(transactions.getFirst())));
            transactionLifecycleService.success(transactions);

            return transactions.getFirst();
        } catch (Exception e) {
            log.error("Subscription charge failed for user: {}. Moving to REJECTED.", request.getUserGuid());

            transactionLifecycleService.rejectSafely(transactions);

            throw e;
        }
    }*/
}
