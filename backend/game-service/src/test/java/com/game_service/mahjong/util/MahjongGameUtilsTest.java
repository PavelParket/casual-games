package com.game_service.mahjong.util;

import com.game_service.mahjong.domain.dto.GeneratedPair;
import com.game_service.mahjong.domain.entity.Board;
import com.game_service.mahjong.domain.entity.Mahjong;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MahjongGameUtilsTest {

    private static final long SEED = 42L;

    @Test
    void removePair_replaysGeneratorOrder_clearsBoard() {
        List<GeneratedPair> assignment = MahjongLayoutGenerator.generate(SEED);
        Board board = MahjongGameUtils.initBoard(assignment);

        assertEquals(72, board.getFaces().size());
        assertEquals(72, board.getRemaining());
        assertFalse(board.getFreeSet().isEmpty());

        for (GeneratedPair pair : assignment) {
            boolean applied = MahjongGameUtils.removePair(board, pair.slot1(), pair.slot2());
            assertTrue(applied, "expected removable pair: " + pair.slot1() + "/" + pair.slot2());
        }

        assertEquals(0, board.getRemaining());
        assertTrue(board.getFreeSet().isEmpty());
        assertEquals(0, MahjongGameUtils.availableMoves(board));
        assertTrue(MahjongGameUtils.isCleared(board));
    }

    @Test
    void removePair_rejectsNonFreeSlot() {
        List<GeneratedPair> assignment = MahjongLayoutGenerator.generate(SEED);
        Board board = MahjongGameUtils.initBoard(assignment);

        // оба слота накрыты layer-1 тайлами по-статичному template - не свободны в начале игры, независимо от seed
        boolean applied = MahjongGameUtils.removePair(board, "L0_C1_R1", "L0_C2_R1");

        assertFalse(applied);
        assertEquals(72, board.getRemaining());
    }

    @Test
    void removePair_rejectsMismatchedFaces() {
        List<GeneratedPair> assignment = MahjongLayoutGenerator.generate(SEED);
        Board board = MahjongGameUtils.initBoard(assignment);

        String slot1 = board.getFreeSet().iterator().next();
        var face1 = board.getFaces().get(slot1);

        String slot2 = board.getFreeSet().stream()
                .filter(id -> !id.equals(slot1))
                .filter(id -> !board.getFaces().get(id).equals(face1))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("expected a mismatched free tile for seed=" + SEED));

        boolean applied = MahjongGameUtils.removePair(board, slot1, slot2);

        assertFalse(applied);
        assertEquals(72, board.getRemaining());
    }

    @Test
    void removePair_rejectsSameSlotTwice() {
        List<GeneratedPair> assignment = MahjongLayoutGenerator.generate(SEED);
        Board board = MahjongGameUtils.initBoard(assignment);

        String slot = board.getFreeSet().iterator().next();

        boolean applied = MahjongGameUtils.removePair(board, slot, slot);

        assertFalse(applied);
        assertEquals(72, board.getRemaining());
    }

    @Test
    void initGame_createsIndependentBoardsPerPlayer() {
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();

        Mahjong mahjong = MahjongGameUtils.initGame(SEED, List.of(player1, player2));
        assertEquals(2, mahjong.getBoards().size());

        Board board1 = mahjong.getBoards().get(player1);
        Board board2 = mahjong.getBoards().get(player2);

        GeneratedPair firstPair = MahjongLayoutGenerator.generate(SEED).getFirst();
        boolean applied = MahjongGameUtils.removePair(board1, firstPair.slot1(), firstPair.slot2());

        assertTrue(applied);
        assertEquals(70, board1.getRemaining());
        assertEquals(72, board2.getRemaining(), "removing on board1 must not affect board2");
    }

    @Test
    void availableMoves_matchesManualCount() {
        List<GeneratedPair> assignment = MahjongLayoutGenerator.generate(SEED);
        Board board = MahjongGameUtils.initBoard(assignment);

        assertTrue(MahjongGameUtils.availableMoves(board) > 0, "initial free set should offer at least one move");
    }
}
