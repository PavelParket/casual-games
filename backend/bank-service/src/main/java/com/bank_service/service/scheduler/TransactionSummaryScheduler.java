package com.bank_service.service.scheduler;

import com.bank_service.domain.dto.GenerateSummaryRequest;
import com.bank_service.service.TransactionSummaryService;
import com.cron_starter.service.CronService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionSummaryScheduler implements CronService {

    private static final String CODE = "transaction-summary-generation";
    private static final String DESCRIPTION = "Generate transaction summaries";

    private final TransactionSummaryService transactionSummaryService;

    @Scheduled(cron = "${cron.create-transaction-summaries}", zone = "UTC")
    @SchedulerLock(lockAtLeastFor = "PT5M", lockAtMostFor = "PT30M", name = CODE)
    public void scheduled() {
        run();
    }

    @Override
    public void run() {
        log.info("Started job: {}", DESCRIPTION);

        LocalDate targetMonth = LocalDate.now(ZoneOffset.UTC).minusMonths(1);

        log.info("Target month: {}", targetMonth);

        transactionSummaryService.generateSummary(
                GenerateSummaryRequest.builder()
                        .targetMonth(targetMonth)
                        .build()
        );

        log.info("Finished job: {}", DESCRIPTION);
    }

    @Override
    public String getCode() {
        return CODE;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }
}
