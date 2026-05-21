package com.bank_service.service;

import com.bank_service.domain.entity.Transaction;
import com.bank_service.domain.enums.TransactionStatus;
import com.bank_service.factory.SubscriptionTransactionFactory;
import com.bank_service.repository.TransactionRepository;
import com.bank_service.service.grpc.client.GrpcUserTransactionClient;
import com.casualgames.grpc.subscription.SubscriptionTransactionRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionTransactionService {

    private final TransactionRepository transactionRepository;

    private final SubscriptionTransactionFactory subscriptionTransactionFactory;

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
    }
}
