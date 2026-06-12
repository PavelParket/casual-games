package com.bank_service.repository;

import com.bank_service.domain.entity.Transaction;
import com.bank_service.domain.enums.TransactionStatus;
import com.bank_service.repository.projection.TransactionSummaryProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
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
    Optional<Transaction> findFirstByUserGuidAndStatusOrderByCreatedAtDesc(UUID userGuid, String status);

    @Query(value = """
            SELECT DISTINCT t.user_guid
            FROM transactions t
            WHERE t.status = :status
            AND t.created_at >= :start
            AND t.created_at < :end
            """,
            countQuery = """
                    SELECT COUNT(DISTINCT t.user_guid)
                    FROM transactions t
                    WHERE t.status = :status
                    AND t.created_at >= :start
                    AND t.created_at < :end
                    """,
            nativeQuery = true)
    Page<UUID> findDistinctUsersWithTransactionsInPeriod(String status, Instant start, Instant end, Pageable pageable);

    @Query(value = """
            SELECT
                COALESCE(SUM(CASE WHEN t.type = 'ADDITION' THEN t.amount ELSE 0 END), 0) AS total_won,
                COALESCE(SUM(CASE WHEN t.type = 'SUBTRACTION' THEN t.amount ELSE 0 END), 0) AS total_lost,
                (SELECT t2.balance_before AS balance_before
                    FROM transactions t2
                    WHERE t2.user_guid = :userGuid
                    AND t2.status = :status
                    AND t2.created_at >= :start
                    AND t2.created_at < :end
                    ORDER BY t2.created_at LIMIT 1),
                (SELECT t2.balance_after AS balance_after
                    FROM transactions t2
                    WHERE t2.user_guid = :userGuid
                    AND t2.status = :status
                    AND t2.created_at >= :start
                    AND t2.created_at < :end
                    ORDER BY t2.created_at DESC LIMIT 1)
            FROM transactions t
            WHERE t.user_guid = :userGuid
            AND t.status = :status
            AND t.created_at >= :start
            AND t.created_at < :end
            """, nativeQuery = true)
    Optional<TransactionSummaryProjection> findUserAggregatedSummary(UUID userGuid, String status, Instant start, Instant end);

    @Query(value = """
            SELECT DISTINCT * FROM transactions t
            WHERE t.type = :type
            AND t.status = :status
            AND t.room_type IS NOT NULL
            AND t.created_at >= :startOfDay
            AND t.created_at < :endOfDay
            ORDER BY t.amount DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Transaction> findTopWinsForDay(String type, String status, Instant startOfDay, Instant endOfDay, int limit);

    @Query(value = """
            SELECT * FROM transactions
            WHERE user_guid = :userGuid
            AND type = 'ADDITION'
            AND room_id IS NULL
            AND status = :status
            ORDER BY created_at DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<Transaction> findLastDeposit(UUID userGuid, String status);
}
