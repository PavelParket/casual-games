package com.bank_service.service.scheduler;

import com.bank_service.domain.dto.GenerateSummaryRequest;
import com.bank_service.service.TransactionSummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneOffset;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionSummaryScheduler {

    private final TransactionSummaryService summaryService;

    @Scheduled(
            initialDelayString = "${app.scheduling.transaction-summary.initial-delay}",
            fixedRateString = "${app.scheduling.transaction-summary.fixed-rate}"
    )
    public void runMonthlySummary() {
        GenerateSummaryRequest request = GenerateSummaryRequest.builder()
                .targetMonth(LocalDate.now(ZoneOffset.UTC).minusMonths(1))
                .build();

        log.info("Scheduler triggered transaction summary generation for {}", request.targetMonth());

        summaryService.generateSummary(request);
    }
}
