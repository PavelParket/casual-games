package com.game_service.durak.utils;

import com.game_service.durak.domain.entity.Durak;
import com.game_service.durak.domain.entity.DurakCard;
import com.game_service.durak.domain.enums.DurakCardRank;
import com.game_service.durak.domain.enums.DurakCardSuit;
import com.game_service.durak.domain.enums.DurakStatus;
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

    private static final Comparator<DurakCard> HAND_ORDER = Comparator.comparing(DurakCard::suit).thenComparing(DurakCard::rank);

    public static Durak initialize(UUID roomId, UUID firstPlayerId, UUID secondPlayerId) {
        List<DurakCard> deck = buildShuffledDeck();

        List<DurakCard> firstHand = new ArrayList<>();
        List<DurakCard> secondHand = new ArrayList<>();

        dealInterleaved(deck, firstHand, secondHand);

        DurakCard trumpCard = deck.getLast();

        firstHand.sort(HAND_ORDER);
        secondHand.sort(HAND_ORDER);

        Map<UUID, List<DurakCard>> hands = new HashMap<>();
        hands.put(firstPlayerId, firstHand);
        hands.put(secondPlayerId, secondHand);

        UUID firstAttacker = determineFirstAttacker(firstPlayerId, firstHand, secondPlayerId, secondHand, trumpCard.suit());
        UUID firstDefender = firstAttacker.equals(firstPlayerId) ? secondPlayerId : firstPlayerId;

        log.info("New durak match initialized: roomId={} trump={} firstAttacker={}", roomId, trumpCard, firstAttacker);

        return Durak.builder()
                .roomId(roomId)
                .status(DurakStatus.STARTED)
                .players(List.of(firstPlayerId, secondPlayerId))
                .deck(deck)
                .trumpCard(trumpCard)
                .trumpSuit(trumpCard.suit())
                .hands(hands)
                .attackerId(firstAttacker)
                .defenderId(firstDefender)
                .currentActorId(firstAttacker)
                .build();
    }

    public static void dealCards(Durak match) {
        replenishHand(match, match.getAttackerId());
        replenishHand(match, match.getDefenderId());

        log.info("Deal cards gameId={} deckRemaining={} attackerCards={} defenderCards={}", match.getId(), match.getDeck().size(), match.attackerHand().size(), match.defenderHand().size());
    }

    public static boolean isGameOver(Durak match) {
        if (!match.isDeckEmpty()) {
            return false;
        }

        return match.getHands().values().stream()
                .anyMatch(List::isEmpty);
    }

    public static Optional<UUID> determineWinner(Durak match) {
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

    private static void dealInterleaved(List<DurakCard> deck, List<DurakCard> firstHand, List<DurakCard> secondHand) {
        for (int batch = 1; batch <= 3; batch++) {
            for (int i = 0; i < batch; i++) {
                firstHand.add(deck.removeFirst());
            }

            for (int i = 0; i < batch; i++) {
                secondHand.add(deck.removeFirst());
            }
        }
    }

    private static List<DurakCard> buildShuffledDeck() {
        List<DurakCard> deck = new ArrayList<>(DECK_SIZE);

        for (DurakCardSuit suit : DurakCardSuit.values()) {
            for (DurakCardRank rank : DurakCardRank.values()) {
                deck.add(new DurakCard(rank, suit));
            }
        }

        Collections.shuffle(deck);

        return deck;
    }

    private static void replenishHand(Durak match, UUID playerId) {
        List<DurakCard> hand = match.getHands().get(playerId);
        List<DurakCard> deck = match.getDeck();

        while (hand.size() < INITIAL_HAND_SIZE && !deck.isEmpty()) {
            hand.add(deck.removeFirst());
        }

        hand.sort(HAND_ORDER);
    }

    private static UUID determineFirstAttacker(UUID firstPlayerId,
                                               List<DurakCard> firstHand,
                                               UUID secondPlayerId,
                                               List<DurakCard> secondHand,
                                               DurakCardSuit trumpSuit) {

        Optional<DurakCard> firstPlayerLowest = lowestTrump(firstHand, trumpSuit);
        Optional<DurakCard> secondPlayerLowest = lowestTrump(secondHand, trumpSuit);

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

    private static Optional<DurakCard> lowestTrump(List<DurakCard> hand, DurakCardSuit trumpSuit) {
        return hand.stream()
                .filter(card -> card.suit() == trumpSuit)
                .min(Comparator.comparingInt(card -> card.rank().strength()));
    }
}
