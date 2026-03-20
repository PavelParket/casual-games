package com.game_service.durak.validator;

import com.game_service.common.exception.InvalidMoveException;
import com.game_service.durak.domain.dto.DurakGameRequest;
import com.game_service.durak.domain.entity.Durak;
import com.game_service.durak.domain.entity.DurakCard;
import com.game_service.durak.domain.entity.DurakTablePair;
import com.game_service.durak.domain.enums.DurakAction;
import com.game_service.durak.domain.enums.DurakPhase;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DurakGameValidator {

    public void validate(Durak game, DurakGameRequest request) {
        if (!request.currentActorId().equals(game.getCurrentActorId())) {
            throw new InvalidMoveException("Not your turn. Current actor: " + game.getCurrentActorId());
        }

        if (request.action() == DurakAction.PLAY_CARD && request.card() == null) {
            throw new InvalidMoveException("PLAY_CARD action requires a card to be specified");
        }

        if (request.action() == DurakAction.PLAY_CARD) {
            List<DurakCard> hand = game.getHands().get(request.currentActorId());

            if (hand == null || !hand.contains(request.card())) {
                throw new InvalidMoveException("Card " + request.card() + " is not in your hand");
            }
        }

        DurakPhase phase = game.getPhase();

        switch (phase) {
            case ATTACKING -> validateAttacking(game, request.action(), request.card());
            case DEFENDING -> validateDefending(game, request.action(), request.card());
            case THROWING_MORE -> validateThrowingMore(game, request.action(), request.card());
            case PICKING_UP -> validatePickingUp(game, request.action(), request.card());
            default -> throw new InvalidMoveException("No moves are allowed in phase: " + phase);
        }
    }

    private void validateAttacking(Durak game, DurakAction action, DurakCard card) {
        switch (action) {
            case PLAY_CARD -> {
                if (game.getTable().isEmpty()) {
                } else {
                    requireRankOnTable(game, card);
                    requireThrowInLimit(game);
                }
            }
            case PASS -> {
                if (game.getTable().isEmpty()) {
                    throw new InvalidMoveException("Cannot declare бита on an empty table");
                }

                boolean allDefended = game.getTable().stream().allMatch(DurakTablePair::isDefended);

                if (!allDefended) {
                    throw new InvalidMoveException("Cannot declare бита: there are undefended cards on the table");
                }
            }
            case TAKE_CARDS -> throw new InvalidMoveException("Attacker cannot take cards");
        }
    }

    private void validateDefending(Durak game, DurakAction action, DurakCard card) {
        switch (action) {
            case PLAY_CARD -> {
                DurakTablePair undefendedPair = game.getTable().stream()
                        .filter(p -> !p.isDefended())
                        .findFirst()
                        .orElseThrow(() -> new InvalidMoveException("No undefended attack card on the table to defend against"));

                if (!card.beats(undefendedPair.attackCard(), game.getTrumpSuit())) {
                    throw new InvalidMoveException("Card " + card + " cannot beat " + undefendedPair.attackCard() + " (trump=" + game.getTrumpSuit() + ")");
                }
            }
            case TAKE_CARDS -> {
            }
            case PASS ->
                    throw new InvalidMoveException("Defender cannot PASS in DEFENDING phase; use TAKE_CARDS to pick up");
        }
    }

    private void validateThrowingMore(Durak game, DurakAction action, DurakCard card) {
        switch (action) {
            case PLAY_CARD -> {
                requireRankOnTable(game, card);
                requireThrowInLimit(game);
            }
            case PASS -> {
            }
            case TAKE_CARDS -> throw new InvalidMoveException("Attacker cannot take cards in THROWING_MORE phase");
        }
    }

    private void validatePickingUp(Durak game, DurakAction action, DurakCard card) {
        switch (action) {
            case PLAY_CARD -> {
                requireRankOnTable(game, card);
                requirePickingUpLimit(game);
            }
            case PASS -> {
            }
            case TAKE_CARDS -> throw new InvalidMoveException("TAKE_CARDS is not valid in PICKING_UP phase");
        }
    }

    private void requireRankOnTable(Durak game, DurakCard card) {
        boolean rankPresent = game.getTable().stream()
                .anyMatch(pair ->
                        pair.attackCard().rank() == card.rank()
                                || (pair.defendCard() != null && pair.defendCard().rank() == card.rank())
                );

        if (!rankPresent) {
            throw new InvalidMoveException("Rank " + card.rank() + " is not represented on the table; cannot throw in");
        }
    }

    private void requireThrowInLimit(Durak game) {
        if (game.defenderHand().isEmpty()) {
            throw new InvalidMoveException("Cannot throw in more cards: defender has no cards left");
        }
    }

    private void requirePickingUpLimit(Durak game) {
        if (game.getTable().size() >= 6) {
            throw new InvalidMoveException("Cannot throw in more cards: table already has 6 pairs, which is the maximum");
        }
    }
}
