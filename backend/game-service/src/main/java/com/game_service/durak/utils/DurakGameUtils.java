package com.game_service.durak.utils;

import com.game_service.durak.domain.entity.Card;
import com.game_service.durak.domain.entity.DurakMatch;
import com.game_service.durak.domain.enums.CardRank;
import com.game_service.durak.domain.enums.CardSuit;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
public class DurakGameUtils {

    private static final int DECK_SIZE = 36;

    private static final int INITIAL_HAND_SIZE = 6;

    public static DurakMatch initialize(UUID roomId, UUID firstPlayerId, UUID secondPlayerId) {
        List<Card> deck = buildShuffledDeck();

        List<Card> firstHand = new ArrayList<>(deck.subList(0, INITIAL_HAND_SIZE));
        List<Card> secondHand = new ArrayList<>(deck.subList(INITIAL_HAND_SIZE, INITIAL_HAND_SIZE * 2));
        List<Card> drawPile = new ArrayList<>(deck.subList(INITIAL_HAND_SIZE * 2, deck.size()));

        Card trumpCard = drawPile.getLast();

        Map<UUID, List<Card>> hands = new HashMap<>() {{
            put(firstPlayerId, firstHand);
            put(secondPlayerId, secondHand);
        }};

        UUID firstAttacker = determineFirstAttacker(firstPlayerId, firstHand, secondPlayerId, secondHand, trumpCard.suit());
        UUID firstDefender = firstAttacker.equals(firstPlayerId) ? secondPlayerId : firstPlayerId;

        log.info("New durak match initialized: roomId={} trump={} firstAttacker={}", roomId, trumpCard, firstAttacker);

        return DurakMatch.builder()
                .gameId(UUID.randomUUID())
                .roomId(roomId)
                .deck(deck)
                .trumpCard(trumpCard)
                .trumpSuit(trumpCard.suit())
                .hands(hands)
                .attackerId(firstAttacker)
                .defenderId(firstDefender)
                .currentActorId(firstAttacker)
                .build();
    }

    public static void dealCards(DurakMatch match) {
        replenishHand(match, match.getAttackerId());
        replenishHand(match, match.getDefenderId());

        log.info("Deal cards gameId={} deckRemaining={} attackerCards={} defenderCards={}", match.getGameId(), match.getDeck().size(), match.attackerHand().size(), match.defenderHand().size());
    }

    public static boolean isGameOver(DurakMatch match) {
        if (!match.isDeckEmpty()) {
            return false;
        }

        return match.getHands().values().stream()
                .anyMatch(List::isEmpty);
    }

    public static Optional<UUID> determineWinner(DurakMatch match) {
        List<UUID> emptyHands = match.getHands().entrySet().stream()
                .filter(hand -> hand.getValue().isEmpty())
                .map(Map.Entry::getKey)
                .toList();

        if (emptyHands.size() == match.getHands().size()) {
            return Optional.empty();
        }

        return emptyHands.stream()
                .findFirst();
    }

    private static List<Card> buildShuffledDeck() {
        List<Card> deck = new ArrayList<>(DECK_SIZE);

        for (CardSuit suit : CardSuit.values()) {
            for (CardRank rank : CardRank.values()) {
                deck.add(new Card(rank, suit));
            }
        }

        Collections.shuffle(deck);

        return deck;
    }

    private static void replenishHand(DurakMatch match, UUID playerId) {
        List<Card> hand = match.getHands().get(playerId);
        List<Card> deck = match.getDeck();

        while (hand.size() < INITIAL_HAND_SIZE && !deck.isEmpty()) {
            hand.add(deck.removeFirst());
        }
    }

    private static UUID determineFirstAttacker(UUID firstPlayerId,
                                               List<Card> firstHand,
                                               UUID secondPlayerId,
                                               List<Card> secondHand,
                                               CardSuit trumpSuit) {

        Optional<Card> firstPlayerLowest = lowestTrump(firstHand, trumpSuit);
        Optional<Card> secondPlayerLowest = lowestTrump(secondHand, trumpSuit);

        if (firstPlayerLowest.isEmpty() && secondPlayerLowest.isEmpty()) {
            return Math.random() < 0.5 ? firstPlayerId : secondPlayerId;
        }

        if (firstPlayerLowest.isEmpty()) {
            return secondPlayerId;
        }

        if (secondPlayerLowest.isEmpty()) {
            return firstPlayerId;
        }

        int compare = Integer.compare(firstPlayerLowest.get().rank().strength(), secondPlayerLowest.get().rank().strength());

        return compare <= 0 ? firstPlayerId : secondPlayerId;
    }

    private static Optional<Card> lowestTrump(List<Card> hand, CardSuit trumpSuit) {
        return hand.stream()
                .filter(card -> card.suit() == trumpSuit)
                .min(Comparator.comparingInt(card -> card.rank().strength()));
    }
}
