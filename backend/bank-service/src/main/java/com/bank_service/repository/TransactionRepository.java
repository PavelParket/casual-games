package com.bank_service.repository;

import com.bank_service.domain.entity.Transaction;
import com.bank_service.domain.enums.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findByUserGuidAndStatus(UUID userGuid,
                                              TransactionStatus status,
                                              Pageable pageable);

    @Query(value = """
            SELECT * FROM transactions t
            WHERE t.user_guid = :userGuid
            AND t.status = :status
            ORDER BY t.created_at DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<Transaction> findFirstByUserGuidAndStatusOrderByCreatedAtDesc(
            @Param("userGuid") UUID userGuid,
            @Param("status") String status
    );

    @Query(value = """
            SELECT DISTINCT t.user_guid
            FROM transactions t
            WHERE t.status = :status
            AND t.created_at >= :start
            AND t.created_at < :end
            """, nativeQuery = true)
    Page<UUID> findDistinctUsersWithTransactionsInPeriod(
            @Param("status") String status,
            @Param("start") Instant start,
            @Param("end") Instant end,
            Pageable pageable
    );

    @Query(value = """
            SELECT *
            FROM transactions t
            WHERE t.user_guid = :userGuid
            AND t.status = :status
            AND t.created_at >= :start
            AND t.created_at < :end
            ORDER BY t.created_at
            """, nativeQuery = true)
    Page<Transaction> findTransactionsForSummary(
            @Param("userGuid") UUID userGuid,
            @Param("status") String status,
            @Param("start") Instant start,
            @Param("end") Instant end,
            Pageable pageable
    );
}
