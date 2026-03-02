package com.bank_service.service;

import com.bank_service.domain.dto.TransactionSummaryFilterRequest;
import com.bank_service.domain.dto.TransactionSummaryResponse;
import com.bank_service.mapper.TransactionSummaryMapper;
import com.bank_service.repository.TransactionSummaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionSummaryService {

    private final int ONE_DAY = 1;

    private final TransactionSummaryRepository summaryRepository;

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
}
