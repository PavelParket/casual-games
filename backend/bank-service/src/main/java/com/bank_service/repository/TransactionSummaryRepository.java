package com.bank_service.repository;

import com.bank_service.domain.entity.TransactionSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionSummaryRepository extends JpaRepository<TransactionSummary, Long> {

    List<TransactionSummary> findByUserGuidAndSummaryMonthBetween(UUID userGuid, LocalDate startDate, LocalDate endDate);
}
