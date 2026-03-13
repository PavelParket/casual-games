package com.game_service.durak.domain.entity;

import com.game_service.durak.domain.enums.CardSuit;
import com.game_service.durak.domain.enums.DurakPhase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class DurakMatch {

    private final UUID gameId;

    private final UUID roomId;

    @Builder.Default
    private DurakPhase phase = DurakPhase.ATTACKING;

    private UUID currentActorId;

    private UUID attackerId;

    private UUID defenderId;

    @Builder.Default
    private List<Card> deck = new ArrayList<>();

    @Builder.Default
    private Map<UUID, List<Card>> hands = new HashMap<>();

    @Builder.Default
    private List<TablePair> table = new ArrayList<>();

    private Card trumpCard;

    private CardSuit trumpSuit;

    @Builder.Default
    private int boutNumber = 0;

    @Builder.Default
    private Instant startedAt = Instant.now();

    @Builder.Default
    private Instant lastActionAt = Instant.now();

    private UUID winnerId;

    public List<Card> currentActorHand() {
        return hands.get(currentActorId);
    }

    public List<Card> attackerHand() {
        return hands.get(attackerId);
    }

    public List<Card> defenderHand() {
        return hands.get(defenderId);
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
