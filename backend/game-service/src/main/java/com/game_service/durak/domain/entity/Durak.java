package com.game_service.durak.domain.entity;

import com.game_service.durak.domain.enums.DurakCardSuit;
import com.game_service.durak.domain.enums.DurakPhase;
import com.game_service.durak.domain.enums.DurakStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "game_durak")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Durak {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private UUID roomId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DurakStatus status;

    private UUID winnerId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private List<UUID> players;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Transient
    @Builder.Default
    private DurakPhase phase = DurakPhase.ATTACKING;

    @Transient
    private UUID currentActorId;

    @Transient
    private UUID attackerId;

    @Transient
    private UUID defenderId;

    @Transient
    @Builder.Default
    private List<DurakCard> deck = new ArrayList<>();

    @Transient
    @Builder.Default
    private Map<UUID, List<DurakCard>> hands = new HashMap<>();

    @Transient
    @Builder.Default
    private List<DurakTablePair> table = new ArrayList<>();

    @Transient
    private DurakCard trumpCard;

    @Transient
    private DurakCardSuit trumpSuit;

    @Transient
    @Builder.Default
    private int boutNumber = 0;

    @Transient
    @Builder.Default
    private Instant lastActionAt = Instant.now();

    public List<DurakCard> currentActorHand() {
        return hands.get(currentActorId);
    }

    public List<DurakCard> attackerHand() {
        return hands.getOrDefault(attackerId, List.of());
    }

    public List<DurakCard> defenderHand() {
        return hands.getOrDefault(defenderId, List.of());
    }

    public boolean isDeckEmpty() {
        return deck.isEmpty();
    }

    public long undefendedCount() {
        return table.stream()
                .filter(pair -> !pair.isDefended())
                .count();
    }
}
