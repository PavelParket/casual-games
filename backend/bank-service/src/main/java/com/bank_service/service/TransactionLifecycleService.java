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
import java.util.Objects;

@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
@RequiredArgsConstructor
@Slf4j
public class TransactionLifecycleService {

    private final TransactionRepository transactionRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void success(List<Transaction> transactions) {
        transactions.forEach(transaction -> transaction.setStatus(TransactionStatus.SUCCESS));
        transactionRepository.saveAll(transactions);

        log.info("Transactions marked as SUCCESS: {}", transactions.size());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void reject(List<Transaction> transactions) {
        transactions.forEach(transaction -> transaction.setStatus(TransactionStatus.REJECTED));
        transactionRepository.saveAll(transactions);

        log.info("Transactions marked as REJECTED: {}", transactions.size());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void rejectSafely(List<Transaction> transactions) {
        try {
            reject(transactions);
        } catch (Exception e) {
            log.error("Failed to reject transactions, attempting recovery", e);

            List<Long> ids = transactions.stream()
                    .map(Transaction::getId)
                    .filter(Objects::nonNull)
                    .toList();

            if (!ids.isEmpty()) {
                List<Transaction> fresh = transactionRepository.findAllById(ids);
                fresh.forEach(transaction -> transaction.setStatus(TransactionStatus.REJECTED));
                transactionRepository.saveAll(fresh);

                log.info("Successfully rejected {} transactions on retry", fresh.size());
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<Transaction> pending(List<Transaction> transactions) {
        transactions.forEach(transaction -> transaction.setStatus(TransactionStatus.PENDING));
        List<Transaction> saved = transactionRepository.saveAll(transactions);

        log.info("Transactions marked as PENDING: {}", transactions.size());

        return saved;
    }
}
