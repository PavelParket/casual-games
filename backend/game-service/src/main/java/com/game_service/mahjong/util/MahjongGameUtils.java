package com.game_service.mahjong.util;

import com.game_service.mahjong.domain.dto.GeneratedPair;
import com.game_service.mahjong.domain.entity.Board;
import com.game_service.mahjong.domain.entity.Mahjong;
import com.game_service.mahjong.domain.entity.Slot;
import com.game_service.mahjong.domain.entity.TileFace;
import com.game_service.mahjong.domain.enums.TileSuit;
import lombok.experimental.UtilityClass;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@UtilityClass
public class MahjongGameUtils {

    private static final long ZERO = 0;
    private static final long ONE_LONG = 1L;
    private static final int TWO_INT = 2;

    public Mahjong initGame(long seed, List<UUID> playersGuids) {
        List<GeneratedPair> assignment = MahjongLayoutGenerator.generate(seed);
        Map<String, TileFace> faces = buildFaces(assignment);

        Map<UUID, Board> boards = new HashMap<>();

        playersGuids.forEach((playerGuid) -> {
            boards.put(playerGuid, buildBoard(faces));
        });

        return Mahjong.builder()
                .seed(seed)
                .boards(boards)
                .build();
    }

    public Board initBoard(List<GeneratedPair> assignment) {
        return buildBoard(buildFaces(assignment));
    }

    public boolean removePair(Board board, String slot1Id, String slot2Id) {
        if (slot1Id.equals(slot2Id)) {
            return false;
        }

        if (!board.getFreeSet().contains(slot1Id) || !board.getFreeSet().contains(slot2Id)) {
            return false;
        }

        TileFace face1 = board.getFaces().get(slot1Id);
        TileFace face2 = board.getFaces().get(slot2Id);

        if (!facesMatch(face1, face2)) {
            return false;
        }

        board.getRemoved().add(slot1Id);
        board.getRemoved().add(slot2Id);
        board.setRemaining(board.getRemaining() - TWO_INT);

        recalculateFreeSet(board, slot1Id, slot2Id);

        return true;
    }

    public int availableMoves(Board board) {
        Map<TileFace, Long> exactCounts = new HashMap<>();
        AtomicInteger flowerCount = new AtomicInteger();
        AtomicInteger seasonCount = new AtomicInteger();

        board.getFreeSet().forEach(slotId -> {
            TileFace face = board.getFaces().get(slotId);

            if (TileSuit.FLOWER.equals(face.getSuit())) {
                flowerCount.getAndIncrement();
            } else if (TileSuit.SEASON.equals(face.getSuit())) {
                seasonCount.getAndIncrement();
            } else {
                exactCounts.merge(face, ONE_LONG, Long::sum);
            }
        });

        AtomicInteger moves = new AtomicInteger(flowerCount.get() / TWO_INT + seasonCount.get() / TWO_INT);

        exactCounts.values().forEach(count -> {
            moves.addAndGet((int) (count / TWO_INT));
        });

        return moves.get();
    }

    public boolean isCleared(Board board) {
        return board.getRemaining() == ZERO;
    }

    public boolean isDeadlocked(Board board) {
        return availableMoves(board) == ZERO;
    }

    private boolean facesMatch(TileFace face1, TileFace face2) {
        if (face1.getSuit() == TileSuit.FLOWER && face2.getSuit() == TileSuit.FLOWER) {
            return true;
        }

        if (face1.getSuit() == TileSuit.SEASON && face2.getSuit() == TileSuit.SEASON) {
            return true;
        }

        return face1.equals(face2);
    }

    private Map<String, TileFace> buildFaces(List<GeneratedPair> assignment) {
        Map<String, TileFace> faces = new HashMap<>();

        assignment.forEach(pair -> {
            faces.put(pair.getSlot1(), pair.getFace());
            faces.put(pair.getSlot2(), pair.getFace());
        });

        return Collections.unmodifiableMap(faces);
    }

    private Board buildBoard(Map<String, TileFace> faces) {
        Set<String> removed = new HashSet<>();
        Set<String> freeSet = new HashSet<>();

        LayoutTemplate.getSlotIds().forEach(slotId -> {
            if (TileRules.isFree(slotId, removed)) {
                freeSet.add(slotId);
            }
        });

        return Board.builder()
                .faces(faces)
                .removed(removed)
                .freeSet(freeSet)
                .remaining(LayoutTemplate.getTotalSlots())
                .build();
    }

    private void recalculateFreeSet(Board board, String slot1Id, String slot2Id) {
        board.getFreeSet().remove(slot1Id);
        board.getFreeSet().remove(slot2Id);

        Set<String> candidates = new HashSet<>();
        collectCandidates(candidates, slot1Id);
        collectCandidates(candidates, slot2Id);

        candidates.forEach(candidateId -> {
            if (!board.getRemoved().contains(candidateId)) {
                if (TileRules.isFree(candidateId, board.getRemoved())) {
                    board.getFreeSet().add(candidateId);
                } else {
                    board.getFreeSet().remove(candidateId);
                }
            }
        });
    }

    private void collectCandidates(Set<String> candidates, String removedSlotId) {
        Slot slot = LayoutTemplate.getSlot(removedSlotId);

        if (slot.getLeftId() != null) {
            candidates.add(slot.getLeftId());
        }

        if (slot.getRightId() != null) {
            candidates.add(slot.getRightId());
        }

        candidates.addAll(LayoutTemplate.getCoveredSlots(removedSlotId));
    }
}
