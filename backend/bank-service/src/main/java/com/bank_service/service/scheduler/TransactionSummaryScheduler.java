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

    @Scheduled(cron = "${cron.create-transaction-summaries}", zone = "UTC")
    public void run() {
        LocalDate targetMonth = LocalDate.now(ZoneOffset.UTC).minusMonths(1);

        log.info("Scheduler start transaction summaries generation for {}", targetMonth);

        summaryService.generateSummary(
                GenerateSummaryRequest.builder()
                        .targetMonth(targetMonth)
                        .build()
        );
    }
}
