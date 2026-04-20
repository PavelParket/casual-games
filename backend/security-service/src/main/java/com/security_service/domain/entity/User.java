package com.security_service.domain.entity;

import com.security_starter.annotation.Permission;
import com.security_starter.enums.Permissions;
import com.security_starter.enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "auths")
@EntityListeners(AuditingEntityListener.class)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Permission(Permissions.GUID)
    @Column
    private UUID guid;

    @Permission(Permissions.USERNAME)
    @Column(nullable = false, length = 50)
    private String username;

    @Permission(Permissions.EMAIL)
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 60)
    private String password;

    @Permission(value = Permissions.ROLE, deleteAllowed = false)
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
