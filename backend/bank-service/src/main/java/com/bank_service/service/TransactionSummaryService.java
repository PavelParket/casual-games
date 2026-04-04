package com.bank_service.service;

import com.bank_service.domain.dto.TransactionSummaryFilterRequest;
import com.bank_service.domain.dto.TransactionSummaryResponse;
import com.bank_service.domain.entity.Transaction;
import com.bank_service.domain.entity.TransactionSummary;
import com.bank_service.domain.enums.TransactionStatus;
import com.bank_service.domain.enums.TransactionType;
import com.bank_service.mapper.TransactionSummaryMapper;
import com.bank_service.repository.TransactionRepository;
import com.bank_service.repository.TransactionSummaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionSummaryService {

    private final int ONE_DAY = 1;
    private final int INITIAL_PAGE = 0;
    private final int DEFAULT_PAGE_SIZE = 100;

    private final TransactionSummaryRepository summaryRepository;

    private final TransactionRepository transactionRepository;

    private final TransactionSummaryMapper summaryMapper;

    @Transactional(readOnly = true)
    public List<TransactionSummaryResponse> getByUserGuid(TransactionSummaryFilterRequest request) {
        LocalDate startDate = request.startDate().withDayOfMonth(ONE_DAY);
        LocalDate endDate = request.endDate() == null ? startDate : request.endDate().withDayOfMonth(ONE_DAY);

        return summaryMapper.toResponseList(summaryRepository.findByUserGuidAndSummaryMonthBetween(
                request.userGuid(),
                startDate,
                endDate
        ));
    }

    public void generateSummary(TransactionSummaryFilterRequest request) {
        LocalDate targetMonth = request.startDate().withDayOfMonth(ONE_DAY);
        LocalDate nextMonth = targetMonth.plusMonths(1);

        Instant startQuery = targetMonth.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant endQuery = nextMonth.atStartOfDay(ZoneOffset.UTC).toInstant();

        log.info("Generating summaries for month {} (DB bounds: >= {} and < {})", targetMonth, startQuery, endQuery);

        if (request.userGuid() != null) {
            processUserSummary(request.userGuid(), targetMonth, startQuery, endQuery);
        } else {
            processAllUsers(targetMonth, startQuery, endQuery);
        }

        log.info("Finished generating transaction summaries for month {}", targetMonth);
    }

    private void processAllUsers(LocalDate summaryMonth, Instant startQuery, Instant endQuery) {
        int page = INITIAL_PAGE;
        Page<UUID> usersPage;

        do {
            usersPage = transactionRepository.findDistinctUsersWithTransactionsInPeriod(
                    TransactionStatus.SUCCESS, startQuery, endQuery, PageRequest.of(page, DEFAULT_PAGE_SIZE)
            );

            for (UUID userGuid : usersPage.getContent()) {
                processUserSummary(userGuid, summaryMonth, startQuery, endQuery);
            }

            page++;
            log.debug("Processed page {}/{} of users", page, usersPage.getTotalPages());
        } while (usersPage.hasNext());
    }

    private void processUserSummary(UUID userGuid, LocalDate summaryMonth, Instant startQuery, Instant endQuery) {
        BigDecimal totalWon = BigDecimal.ZERO;
        BigDecimal totalLost = BigDecimal.ZERO;
        BigDecimal balanceBefore = null;
        BigDecimal balanceAfter = null;

        int page = INITIAL_PAGE;
        Page<Transaction> transactionPage;

        do {
            transactionPage = transactionRepository.findTransactionsForSummary(
                    userGuid, TransactionStatus.SUCCESS, startQuery, endQuery, PageRequest.of(page, DEFAULT_PAGE_SIZE)
            );

            if (transactionPage.hasContent()) {
                List<Transaction> content = transactionPage.getContent();

                if (balanceBefore == null) {
                    balanceBefore = content.getFirst().getBalanceBefore();
                }

                balanceAfter = content.getLast().getBalanceAfter();

                for (Transaction t : content) {
                    if (t.getType() == TransactionType.ADDITION) {
                        totalWon = totalWon.add(t.getAmount());
                    } else if (t.getType() == TransactionType.SUBTRACTION) {
                        totalLost = totalLost.add(t.getAmount());
                    }
                }
            }

            page++;
        } while (transactionPage.hasNext());

        if (balanceBefore == null) {
            return;
        }

        BigDecimal netProfit = totalWon.subtract(totalLost);

        saveSummary(userGuid, summaryMonth, balanceBefore, balanceAfter, totalWon, totalLost, netProfit);
    }

    private void saveSummary(UUID userGuid, LocalDate summaryMonth,
                             BigDecimal balanceBefore, BigDecimal balanceAfter,
                             BigDecimal totalWon, BigDecimal totalLost, BigDecimal netProfit) {

        TransactionSummary summary = summaryRepository.findByUserGuidAndSummaryMonthBetween(userGuid, summaryMonth, summaryMonth)
                .stream()
                .findFirst()
                .orElse(TransactionSummary.builder()
                        .userGuid(userGuid)
                        .summaryMonth(summaryMonth)
                        .build());

        summary.setBalanceBefore(balanceBefore);
        summary.setBalanceAfter(balanceAfter);
        summary.setTotalWon(totalWon);
        summary.setTotalLost(totalLost);
        summary.setNetProfit(netProfit);

        summaryRepository.save(summary);
    }
}
