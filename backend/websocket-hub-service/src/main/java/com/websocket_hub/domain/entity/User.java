package com.websocket_hub.domain.entity;

import com.security_starter.enums.Status;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {

    @Id
    private Long id;

    private UUID guid;

    private String username;

    @Enumerated(EnumType.STRING)
    private Status status;

    private String linkProfilePicture;

    private String linkProfilePictureMini;

    private Instant createdAt;
}
