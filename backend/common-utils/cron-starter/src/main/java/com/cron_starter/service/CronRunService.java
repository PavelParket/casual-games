package com.cron_starter.service;

import com.common_utils.exception.ConflictException;
import com.common_utils.exception.NotFoundException;
import com.common_utils.exception.ServiceUnavailableException;
import com.cron_starter.model.CronJobDescriptor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.core.SimpleLock;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CronRunService {

    private static final Duration LOCK_AT_MOST_FOR = Duration.ofMinutes(30);
    private static final Duration LOCK_AT_LEAST_FOR = Duration.ZERO;

    private final Map<String, CronService> cronServiceMap;

    private final LockProvider lockProvider;

    public CronRunService(Collection<CronService> cronServices, LockProvider lockProvider) {
        this.cronServiceMap = cronServices.stream()
                .collect(
                        Collectors.toMap(
                                CronService::getCode,
                                Function.identity()
                        )
                );
        this.lockProvider = lockProvider;
    }

    public List<CronJobDescriptor> list() {
        return cronServiceMap.values()
                .stream()
                .map(cronService -> CronJobDescriptor.builder()
                        .code(cronService.getCode())
                        .description(cronService.getDescription())
                        .build()
                )
                .toList();
    }

    public void run(String code) {
        CronService cronService = cronServiceMap.get(code);

        if (cronService == null) {
            throw new NotFoundException("Not found job: " + code);
        }

        SimpleLock lock = acquireLock(code)
                .orElseThrow(() -> new ConflictException("Job already running: " + code));

        try {
            cronService.run();
        } catch (RuntimeException e) {
            log.error("Failed job: {}", cronService.getDescription(), e);
            throw e;
        } finally {
            lock.unlock();
        }
    }

    private Optional<SimpleLock> acquireLock(String code) {
        try {
            return lockProvider.lock(new LockConfiguration(Instant.now(), code, LOCK_AT_MOST_FOR, LOCK_AT_LEAST_FOR));
        } catch (DataAccessException e) {
            throw new ServiceUnavailableException("Lock provider is unavailable");
        }
    }
}
