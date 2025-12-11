package com.bank_service.service;

import com.bank_service.domain.entity.Transaction;
import com.bank_service.domain.enums.TransactionStatus;
import com.bank_service.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void success(List<Transaction> transactions) {
        transactions.forEach(transaction -> transaction.setStatus(TransactionStatus.SUCCESS));
        transactionRepository.saveAll(transactions);

        log.info("Transactions marked as SUCCESS: {}", transactions.size());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void reject(List<Transaction> transactions) {
        List<Long> ids = transactions.stream()
                .map(Transaction::getId)
                .toList();

        List<Transaction> updated = transactionRepository.findAllById(ids).stream()
                .map(transaction -> {
                    transaction.setStatus(TransactionStatus.REJECTED);
                    return transaction;
                })
                .toList();

        transactionRepository.saveAll(updated);

        log.info("Transactions marked as REJECTED: {}", transactions.size());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<Transaction> pending(List<Transaction> transactions) {
        transactions.forEach(transaction -> transaction.setStatus(TransactionStatus.PENDING));
        List<Transaction> saved = transactionRepository.saveAll(transactions);

        log.info("Transactions marked as PENDING: {}", transactions.size());

        return saved;
    }
}
