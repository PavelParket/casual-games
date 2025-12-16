package com.bank_service.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "transaction_summaries")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "transaction_summary_seq")
    @SequenceGenerator(
            name = "transaction_summary_seq",
            sequenceName = "transaction_summaries_id_seq",
            allocationSize = 10
    )
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private UUID userGuid;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceBefore;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceAfter;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalWon;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalLost;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal netProfit;

    @Column(nullable = false)
    private LocalDate summaryMonth;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
