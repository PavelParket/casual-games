package com.game_service.mahjong.util;

import com.game_service.mahjong.domain.dto.GeneratedPair;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MahjongLayoutGeneratorTest {

    @Test
    void generate_isSolvableFor1000Seeds() {
        int maxSeed = 1000;

        for (long seed = 1; seed <= maxSeed; seed++) {
            List<GeneratedPair> order = MahjongLayoutGenerator.generate(seed);
            Set<String> removed = new HashSet<>();

            for (GeneratedPair pair : order) {
                assertTrue(TileRules.isFree(pair.slot1(), removed), "seed=" + seed + " slot=" + pair.slot1());
                assertTrue(TileRules.isFree(pair.slot2(), removed), "seed=" + seed + " slot=" + pair.slot2());

                removed.add(pair.slot1());
                removed.add(pair.slot2());
            }

            assertEquals(LayoutTemplate.getTotalSlots(), removed.size(), "seed=" + seed);
        }

        System.out.printf("Mahjong layout: %d/%d seeds generated and verified (slots=%d, maxRetries not exceeded)%n", maxSeed, maxSeed, LayoutTemplate.getTotalSlots());
    }
}
