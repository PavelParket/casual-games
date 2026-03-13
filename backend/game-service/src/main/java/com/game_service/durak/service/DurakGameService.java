package com.game_service.durak.service;

import com.game_service.durak.domain.entity.Card;
import com.game_service.durak.domain.entity.DurakMatch;
import com.game_service.durak.domain.entity.TablePair;
import com.game_service.durak.domain.enums.DurakAction;
import com.game_service.durak.domain.enums.DurakPhase;
import com.game_service.durak.utils.DurakGameUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DurakGameService {

    public void applyMove(DurakMatch match, UUID playerId, DurakAction action, Card card) {
        switch (match.getPhase()) {
            case ATTACKING -> applyAttacking(match, action, card);
            case DEFENDING -> applyDefending(match, action, card);
            case THROWING_MORE -> applyThrowingMore(match, action, card);
            case PICKING_UP -> applyPickingUp(match, action, card);
            default -> throw new IllegalStateException("Cannot apply non-playable phase: " + match.getPhase());
        }

        match.setLastActionAt(Instant.now());

        log.debug("Move applied gameId={} newPhase={} boutNumber={}", match.getGameId(), match.getPhase(), match.getBoutNumber());
    }

    public List<DurakAction> getAvailableActions(DurakMatch match, UUID playerId) {
        if (!playerId.equals(match.getCurrentActorId())) {
            return List.of();
        }

        return switch (match.getPhase()) {
            case ATTACKING -> availableAttackingActions(match);
            case DEFENDING -> List.of(DurakAction.PLAY_CARD, DurakAction.TAKE_CARDS);
            case THROWING_MORE, PICKING_UP -> List.of(DurakAction.PLAY_CARD, DurakAction.PASS);
            default -> List.of();
        };
    }

    private void applyAttacking(DurakMatch match, DurakAction action, Card card) {
        switch (action) {
            case PLAY_CARD -> {
                removeFromHand(match, match.getAttackerId(), card);
                match.getTable().add(TablePair.attack(card));
                transition(match, DurakPhase.DEFENDING, match.getDefenderId());
            }
            case PASS -> executeBoutEnd(match, false);
        }
    }

    private void applyDefending(DurakMatch match, DurakAction action, Card card) {
        switch (action) {
            case PLAY_CARD -> {
                removeFromHand(match, match.getDefenderId(), card);

                List<TablePair> table = match.getTable();

                for (int i = 0; i < table.size(); i++) {
                    if (!table.get(i).isDefended()) {
                        table.set(i, table.get(i).withDefend(card));
                        break;
                    }
                }

                boolean allDefended = table.stream().allMatch(TablePair::isDefended);

                if (allDefended) {
                    transition(match, DurakPhase.THROWING_MORE, match.getAttackerId());
                }
            }
            case TAKE_CARDS -> transition(match, DurakPhase.PICKING_UP, match.getAttackerId());
        }
    }

    private void applyThrowingMore(DurakMatch match, DurakAction action, Card card) {
        switch (action) {
            case PLAY_CARD -> {
                removeFromHand(match, match.getAttackerId(), card);
                match.getTable().add(TablePair.attack(card));
                transition(match, DurakPhase.DEFENDING, match.getDefenderId());
            }
            case PASS -> executeBoutEnd(match, false);
        }
    }

    private void applyPickingUp(DurakMatch match, DurakAction action, Card card) {
        switch (action) {
            case PLAY_CARD -> {
                removeFromHand(match, match.getAttackerId(), card);
                match.getTable().add(TablePair.attack(card));
            }
            case PASS -> {
                transferTableCardsToDefender(match);
                executeBoutEnd(match, true);
            }
        }
    }

    private void executeBoutEnd(DurakMatch match, boolean defenderTookCards) {
        match.getTable().clear();
        DurakGameUtils.dealCards(match);

        if (DurakGameUtils.isGameOver(match)) {
            finalizeGameOver(match);
            return;
        }

        match.setBoutNumber(match.getBoutNumber() + 1);

        if (defenderTookCards) {
            transition(match, DurakPhase.ATTACKING, match.getAttackerId());
        } else {
            swapRolesAndAttack(match);
        }
    }

    private void transferTableCardsToDefender(DurakMatch match) {
        List<Card> defenderHand = match.defenderHand();

        for (TablePair pair : match.getTable()) {
            defenderHand.add(pair.attackCard());

            if (pair.defendCard() != null) {
                defenderHand.add(pair.defendCard());
            }
        }
    }

    private void swapRolesAndAttack(DurakMatch match) {
        UUID newAttacker = match.getDefenderId();
        UUID newDefender = match.getAttackerId();
        match.setAttackerId(newAttacker);
        match.setDefenderId(newDefender);
        transition(match, DurakPhase.ATTACKING, newAttacker);
    }

    private void finalizeGameOver(DurakMatch match) {
        Optional<UUID> winner = DurakGameUtils.determineWinner(match);
        match.setWinnerId(winner.orElse(null));
        match.setPhase(DurakPhase.GAME_OVER);

        log.info("Game over gameId={} winner={} boutNumber={}", match.getGameId(), match.getWinnerId(), match.getBoutNumber());
    }

    private void transition(DurakMatch match, DurakPhase newPhase, UUID nextActor) {
        match.setPhase(newPhase);
        match.setCurrentActorId(nextActor);
    }

    private void removeFromHand(DurakMatch match, UUID playerId, Card card) {
        List<Card> hand = match.getHands().get(playerId);
        boolean removed = hand.remove(card);

        if (!removed) {
            throw new IllegalStateException("Card " + card + " not found in hand of player " + playerId);
        }
    }

    private List<DurakAction> availableAttackingActions(DurakMatch match) {
        List<DurakAction> actions = new ArrayList<>();
        actions.add(DurakAction.PLAY_CARD);

        boolean canPass = !match.getTable().isEmpty() && match.getTable().stream().allMatch(TablePair::isDefended);

        if (canPass) {
            actions.add(DurakAction.PASS);

        }
        return actions;
    }
}
