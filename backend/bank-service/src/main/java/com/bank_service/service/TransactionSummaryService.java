package com.bank_service.service;

import com.bank_service.domain.dto.GenerateSummaryRequest;
import com.bank_service.domain.dto.TransactionSummaryFilterRequest;
import com.bank_service.domain.dto.TransactionSummaryResponse;
import com.bank_service.domain.entity.TransactionSummary;
import com.bank_service.domain.enums.TransactionStatus;
import com.bank_service.mapper.TransactionSummaryMapper;
import com.bank_service.repository.TransactionRepository;
import com.bank_service.repository.TransactionSummaryRepository;
import com.bank_service.repository.projection.TransactionSummaryProjection;
import com.bank_service.service.helper.PermissionHelper;
import com.common_utils.exception.ForbiddenException;
import com.security_starter.enums.Operation;
import com.security_starter.enums.Permissions;
import com.security_starter.validator.PermissionValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static com.bank_service.config.ResourceMessageConstants.FORBIDDEN_READ_SUMMARY;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionSummaryService {

    private final static int ONE_DAY = 1;
    private final static int INITIAL_PAGE = 0;
    private final static int PAGE_SIZE = 100;

    private final TransactionSummaryRepository summaryRepository;

    private final TransactionRepository transactionRepository;

    private final TransactionSummaryMapper summaryMapper;

    private final PermissionHelper permissionHelper;

    private final PermissionValidator permissionValidator;

    @Transactional(readOnly = true)
    public List<TransactionSummaryResponse> getByUserGuid(TransactionSummaryFilterRequest request) {
        if (!permissionValidator.can(
                Permissions.TRANSACTION_SUMMARY,
                Operation.READ,
                permissionHelper.getContext(request.userGuid()),
                permissionHelper.getToken()
        )) {
            throw new ForbiddenException(String.format(FORBIDDEN_READ_SUMMARY, request.userGuid()));
        }

        LocalDate startDate = request.startDate().withDayOfMonth(ONE_DAY);
        LocalDate endDate = request.endDate() == null ? startDate : request.endDate().withDayOfMonth(ONE_DAY);

        return summaryMapper.toResponseList(summaryRepository.findByUserGuidAndSummaryMonthBetween(
                request.userGuid(),
                startDate,
                endDate
        ));
    }

    public void generateSummary(GenerateSummaryRequest request) {
        LocalDate targetMonth = request.targetMonth().withDayOfMonth(ONE_DAY);
        LocalDate nextMonth = targetMonth.plusMonths(1);

        Instant start = targetMonth.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end = nextMonth.atStartOfDay(ZoneOffset.UTC).toInstant();

        log.info("Generating summaries for month {} (bounds: >= {} and < {})", targetMonth, start, end);

        int page = INITIAL_PAGE;
        Page<UUID> usersPage;

        do {
            usersPage = transactionRepository.findDistinctUsersWithTransactionsInPeriod(
                    TransactionStatus.SUCCESS.name(), start, end, PageRequest.of(page, PAGE_SIZE));

            usersPage.getContent().forEach(userGuid -> createSummaryForUser(userGuid, targetMonth, start, end));

            page++;
        } while (usersPage.hasNext());

        log.info("Finished generating transaction summaries for month {}", targetMonth);
    }

    private void createSummaryForUser(UUID userGuid, LocalDate summaryMonth, Instant start, Instant end) {
        transactionRepository.findUserAggregatedSummary(userGuid, TransactionStatus.SUCCESS.name(), start, end)
                .ifPresentOrElse(summaryProjection -> {
                            if (summaryProjection.getBalanceBefore() == null) {
                                log.warn("Aggregation returned null balanceBefore for user {} month {}. Skipping.", userGuid, summaryMonth);
                                return;
                            }
                            save(userGuid, summaryMonth, summaryProjection);
                        },
                        () -> log.warn("No aggregation result for user {} month {}. Skipping.", userGuid, summaryMonth)
                );
    }

    private void save(UUID userGuid, LocalDate summaryMonth, TransactionSummaryProjection summaryProjection) {
        TransactionSummary summary = summaryRepository.findByUserGuidAndSummaryMonth(userGuid, summaryMonth)
                .orElse(TransactionSummary.builder()
                        .userGuid(userGuid)
                        .summaryMonth(summaryMonth)
                        .build());

        summary.setBalanceBefore(summaryProjection.getBalanceBefore());
        summary.setBalanceAfter(summaryProjection.getBalanceAfter());
        summary.setTotalWon(summaryProjection.getTotalWon());
        summary.setTotalLost(summaryProjection.getTotalLost());
        summary.setNetProfit(summaryProjection.getTotalWon().subtract(summaryProjection.getTotalLost()));

        summaryRepository.save(summary);
    }
}
