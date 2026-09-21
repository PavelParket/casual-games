package com.casualgames.bankservice.service;

import com.casualgames.bankservice.domain.dto.DepositRequest;
import com.casualgames.bankservice.domain.dto.TransactionResponse;
import com.casualgames.bankservice.domain.dto.TransactionResponseList;
import com.casualgames.bankservice.domain.entity.Transaction;
import com.casualgames.bankservice.domain.enums.TransactionStatus;
import com.casualgames.bankservice.domain.enums.TransactionType;
import com.casualgames.bankservice.factory.DefaultTransactionFactory;
import com.casualgames.bankservice.mapper.TransactionMapper;
import com.casualgames.bankservice.repository.TransactionRepository;
import com.casualgames.bankservice.service.grpc.client.GrpcUserTransactionClient;
import com.casualgames.bankservice.service.helper.PermissionHelper;
import com.casualgames.commonutils.exception.BadRequestException;
import com.casualgames.commonutils.exception.ForbiddenException;
import com.casualgames.securitystarter.config.AuthenticationToken;
import com.casualgames.securitystarter.enums.Operation;
import com.casualgames.securitystarter.enums.Permissions;
import com.casualgames.securitystarter.validator.PermissionValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static com.casualgames.bankservice.config.ResourceMessageConstants.BAD_REQUEST_DEPOSIT_COOLDOWN;
import static com.casualgames.bankservice.config.ResourceMessageConstants.DEPOSIT_EXCEEDS_MAX_BALANCE;
import static com.casualgames.bankservice.config.ResourceMessageConstants.FORBIDDEN_DEPOSIT;
import static com.casualgames.bankservice.config.ResourceMessageConstants.FORBIDDEN_READ_TRANSACTIONS;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private static final BigDecimal MAX_DEPOSIT_BALANCE = new BigDecimal("5000");
    private static final Duration DEPOSIT_COOLDOWN = Duration.ofHours(1);
    private static final int SIXTY_SECONDS = 60;
    private static final int ONE_DAY = 1;

    private final TransactionLifecycleService transactionLifecycleService;

    private final TransactionRepository transactionRepository;

    private final TransactionMapper transactionMapper;

    private final GrpcUserTransactionClient grpcUserTransactionClient;

    private final DefaultTransactionFactory defaultTransactionFactory;

    private final PermissionHelper permissionHelper;

    private final PermissionValidator permissionValidator;

    @Transactional(readOnly = true)
    public TransactionResponseList getByUserGuid(UUID userGuid, Pageable pageable, AuthenticationToken token) {
        if (!permissionValidator.hasAccess(Permissions.TRANSACTION, Operation.READ, permissionHelper.getContext(userGuid, token), token)) {
            throw new ForbiddenException(String.format(FORBIDDEN_READ_TRANSACTIONS, userGuid));
        }

        Page<Transaction> transactions = transactionRepository.findByUserGuidAndStatus(userGuid, TransactionStatus.SUCCESS, pageable);

        return TransactionResponseList.builder()
                .transactions(transactionMapper.toPagedModel(transactions, transactionMapper::toResponse))
                .build();
    }

    @Transactional
    public TransactionResponse processDeposit(DepositRequest request, AuthenticationToken token) {
        if (!permissionValidator.hasAccess(
                Permissions.BALANCE,
                Operation.UPDATE,
                permissionHelper.getContext(request.userGuid(), token),
                token
        )) {
            throw new ForbiddenException(String.format(FORBIDDEN_DEPOSIT, request.userGuid()));
        }

        transactionRepository.findLastDeposit(request.userGuid(), TransactionStatus.SUCCESS.name())
                .ifPresent(transaction -> {
                    Instant nextAllowedAt = transaction.getCreatedAt().plus(DEPOSIT_COOLDOWN);
                    if (nextAllowedAt.isAfter(Instant.now())) {
                        long remainingSeconds = Duration.between(Instant.now(), nextAllowedAt).getSeconds();

                        throw new BadRequestException(
                                String.format(
                                        BAD_REQUEST_DEPOSIT_COOLDOWN,
                                        remainingSeconds / SIXTY_SECONDS,
                                        remainingSeconds % SIXTY_SECONDS
                                )
                        );
                    }
                });

        BigDecimal balanceBefore = transactionRepository.findFirstByUserGuidAndStatusOrderByCreatedAtDesc(request.userGuid(), TransactionStatus.SUCCESS.name())
                .map(Transaction::getBalanceAfter)
                .orElse(BigDecimal.ZERO);

        if (balanceBefore.add(request.amount()).compareTo(MAX_DEPOSIT_BALANCE) > 0) {
            throw new BadRequestException(String.format(DEPOSIT_EXCEEDS_MAX_BALANCE, MAX_DEPOSIT_BALANCE));
        }

        Transaction transaction = defaultTransactionFactory.createTransaction(request, balanceBefore);

        List<Transaction> pendingTransactions = transactionLifecycleService.pending(List.of(transaction));

        try {
            grpcUserTransactionClient.sendUpdates(pendingTransactions);

            transactionLifecycleService.success(pendingTransactions);

            return transactionMapper.toResponse(pendingTransactions.getFirst());

        } catch (Exception e) {
            log.error("Deposit failed for user: {}. Moving to REJECTED.", request.userGuid());

            transactionLifecycleService.rejectSafely(pendingTransactions);

            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTopWins(int limit) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        Instant startOfDay = today.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant endOfDay = today.plusDays(ONE_DAY).atStartOfDay(ZoneOffset.UTC).toInstant();

        List<Transaction> topTransactions = transactionRepository.findTopWinsForDay(
                TransactionType.ADDITION.name(),
                TransactionStatus.SUCCESS.name(),
                startOfDay,
                endOfDay,
                limit
        );

        return transactionMapper.toResponseList(topTransactions);
    }
}
