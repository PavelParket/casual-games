package com.game_service.tic_tac_toe.entity;

import com.game_service.tic_tac_toe.enums.TicTacToeGameEvent;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "game_tic_tac_toe")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicTacToeGame {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID roomId;

    @Column(name = "player_x_id", nullable = false)
    private UUID playerXId;

    @Column(name = "player_o_id", nullable = false)
    private UUID playerOId;

    @Transient
    private UUID currentTurnUserId;

    @Column
    private UUID winnerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicTacToeGameEvent event;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String[] board;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<UUID, String> players;

    @Version
    private Long version;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}