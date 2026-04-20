package com.security_service.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_permission")
@EntityListeners(AuditingEntityListener.class)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private UUID userGuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;

    @Column(nullable = false)
    private Boolean forMe;

    @Column(nullable = false)
    private Boolean forAll;

    /**
     * true  = allow (extend role permission)
     * false = restrict (restrict role permission)
     */
    @Column(nullable = false)
    private Boolean allowed;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
