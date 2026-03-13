package com.game_service.durak.validator;

import com.game_service.common.exception.InvalidMoveException;
import com.game_service.durak.domain.entity.Card;
import com.game_service.durak.domain.entity.Durak;
import com.game_service.durak.domain.entity.TablePair;
import com.game_service.durak.domain.enums.DurakAction;
import com.game_service.durak.domain.enums.DurakEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class DurakGameValidator {

    public void validate(Durak match, UUID playerId, DurakAction action, Card card) {
        if (!playerId.equals(match.getCurrentActorId())) {
            throw new InvalidMoveException("Not your turn. Current actor: " + match.getCurrentActorId());
        }

        if (action == DurakAction.PLAY_CARD && card == null) {
            throw new InvalidMoveException("PLAY_CARD action requires a card to be specified");
        }

        if (action == DurakAction.PLAY_CARD) {
            List<Card> hand = match.getHands().get(playerId);

            if (hand == null || !hand.contains(card)) {
                throw new InvalidMoveException("Card " + card + " is not in your hand");
            }
        }

        DurakEvent phase = match.getPhase();

        switch (phase) {
            case ATTACKING -> validateAttacking(match, action, card);
            case DEFENDING -> validateDefending(match, action, card);
            case THROWING_MORE -> validateThrowingMore(match, action, card);
            case PICKING_UP -> validatePickingUp(match, action, card);
            default -> throw new InvalidMoveException("No moves are allowed in phase: " + phase);
        }
    }

    private void validateAttacking(Durak match, DurakAction action, Card card) {
        switch (action) {
            case PLAY_CARD -> {
                if (match.getTable().isEmpty()) {
                } else {
                    requireRankOnTable(match, card);
                    requireThrowInLimit(match);
                }
            }
            case PASS -> {
                if (match.getTable().isEmpty()) {
                    throw new InvalidMoveException("Cannot declare бита on an empty table");
                }

                boolean allDefended = match.getTable().stream().allMatch(TablePair::isDefended);

                if (!allDefended) {
                    throw new InvalidMoveException("Cannot declare бита: there are undefended cards on the table");
                }
            }
            case TAKE_CARDS -> throw new InvalidMoveException("Attacker cannot take cards");
        }
    }

    private void validateDefending(Durak match, DurakAction action, Card card) {
        switch (action) {
            case PLAY_CARD -> {
                TablePair undefendedPair = match.getTable().stream()
                        .filter(p -> !p.isDefended())
                        .findFirst()
                        .orElseThrow(() -> new InvalidMoveException("No undefended attack card on the table to defend against"));

                if (!card.beats(undefendedPair.attackCard(), match.getTrumpSuit())) {
                    throw new InvalidMoveException("Card " + card + " cannot beat " + undefendedPair.attackCard() + " (trump=" + match.getTrumpSuit() + ")");
                }
            }
            case TAKE_CARDS -> {
            }
            case PASS ->
                    throw new InvalidMoveException("Defender cannot PASS in DEFENDING phase; use TAKE_CARDS to pick up");
        }
    }

    private void validateThrowingMore(Durak match, DurakAction action, Card card) {
        switch (action) {
            case PLAY_CARD -> {
                requireRankOnTable(match, card);
                requireThrowInLimit(match);
            }
            case PASS -> {
            }
            case TAKE_CARDS -> throw new InvalidMoveException("Attacker cannot take cards in THROWING_MORE phase");
        }
    }

    private void validatePickingUp(Durak match, DurakAction action, Card card) {
        switch (action) {
            case PLAY_CARD -> {
                requireRankOnTable(match, card);
                requireThrowInLimit(match);
            }
            case PASS -> {
            }
            case TAKE_CARDS -> throw new InvalidMoveException("TAKE_CARDS is not valid in PICKING_UP phase");
        }
    }

    private void requireRankOnTable(Durak match, Card card) {
        boolean rankPresent = match.getTable().stream()
                .anyMatch(pair ->
                        pair.attackCard().rank() == card.rank()
                                || (pair.defendCard() != null && pair.defendCard().rank() == card.rank())
                );

        if (!rankPresent) {
            throw new InvalidMoveException("Rank " + card.rank() + " is not represented on the table; cannot throw in");
        }
    }

    private void requireThrowInLimit(Durak match) {
        int defenderHandSize = match.defenderHand().size();
        int maxAllowed = Math.min(6, defenderHandSize);

        if (match.getTable().size() >= maxAllowed) {
            throw new InvalidMoveException(
                    "Cannot throw in more cards: table has " + match.getTable().size()
                            + " pairs, limit is " + maxAllowed
                            + " (min of 6 and defender hand size " + defenderHandSize + ")");
        }
    }
}
