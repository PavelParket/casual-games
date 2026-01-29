package com.bank_service.repository;

import com.bank_service.domain.entity.Transaction;
import com.bank_service.domain.enums.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findByUserGuidAndStatus(UUID userGuid, TransactionStatus status, Pageable pageable);

    @Query(value = """
            SELECT * FROM transactions t
            WHERE t.user_guid = :userGuid
            AND t.status = :#{#status.name()}
            ORDER BY t.created_at DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<Transaction> findFirstByUserGuidAndStatusOrderByCreatedAtDesc(
            @Param("userGuid") UUID userGuid,
            @Param("status") TransactionStatus status
    );
}
