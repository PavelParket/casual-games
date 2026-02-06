package com.bank_service.service;

import com.bank_service.client.UserServiceClient;
import com.bank_service.domain.dto.DepositRequest;
import com.bank_service.domain.dto.PageResponse;
import com.bank_service.domain.dto.TransactionResponse;
import com.bank_service.domain.entity.Transaction;
import com.bank_service.domain.enums.TransactionStatus;
import com.bank_service.factory.DefaultTransactionFactory;
import com.bank_service.mapper.TransactionMapper;
import com.bank_service.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;

    private final TransactionMapper transactionMapper;

    private final UserServiceClient userServiceClient;

    private final DefaultTransactionFactory defaultTransactionFactory;

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

    @Transactional(readOnly = true)
    public PageResponse<TransactionResponse> getByUserGuid(UUID userGuid, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Transaction> transactions = transactionRepository.findByUserGuidAndStatus(userGuid, TransactionStatus.SUCCESS, pageable);

        log.info("Found {} transactions for user: {} (page {}/{})",
                transactions.getNumberOfElements(), userGuid, page + 1, transactions.getTotalPages());

        return PageResponse.of(transactions.map(transactionMapper::toResponse));
    }

    @Transactional
    public TransactionResponse processDeposit(DepositRequest request) {
        BigDecimal balanceBefore = transactionRepository
                .findFirstByUserGuidAndStatusOrderByCreatedAtDesc(request.userGuid(), TransactionStatus.SUCCESS)
                .map(Transaction::getBalanceAfter)
                .orElse(BigDecimal.ZERO);

        Transaction transaction = defaultTransactionFactory.createTransaction(request, balanceBefore);

        List<Transaction> pendingTransactions = pending(List.of(transaction));

        try {
            userServiceClient.sendUpdates(transactionMapper.toShortInfoList(pendingTransactions));

            success(pendingTransactions);

            return transactionMapper.toResponse(pendingTransactions.getFirst());

        } catch (Exception e) {
            log.error("Deposit failed for user: {}. Moving to REJECTED.", request.userGuid());
            this.rejectSafely(pendingTransactions);
            throw e;
        }
    }
}
