package com.bank_service.repository;

import com.bank_service.domain.entity.Transaction;
import com.bank_service.domain.enums.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findByUserGuidAndStatus(UUID userGuid, TransactionStatus status, Pageable pageable);
}
