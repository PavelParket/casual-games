package com.game_service.mahjong.util;

import com.game_service.common.exception.GameInternalException;
import com.game_service.mahjong.domain.dto.GeneratedPair;
import com.game_service.mahjong.domain.entity.TileFace;
import com.game_service.mahjong.domain.enums.TileSuit;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

@UtilityClass
@Slf4j
public class MahjongLayoutGenerator {

    private static final int MAX_RETRIES = 100;

    public List<GeneratedPair> generate(long seed) {
        Random random = new Random(seed);

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            List<GeneratedPair> generatedPairs = tryFill(random);

            if (generatedPairs != null) {
                log.debug("Mahjong layout generated: seed={} attempts={}", seed, attempt);
                return generatedPairs;
            }
        }

        throw new GameInternalException("Mahjong layout generation exhausted retries: seed=" + seed);
    }

    private List<GeneratedPair> tryFill(Random random) {
        Set<String> assigned = new HashSet<>();
        List<GeneratedPair> order = new ArrayList<>();

        List<TileFace> facePool = shuffledFacePool(random);
        int poolIndex = 0;

        while (assigned.size() < LayoutTemplate.getTotalSlots()) {
            List<String> tiles = new ArrayList<>();

            LayoutTemplate.getSlotIds().forEach(slotId -> {
                if (!assigned.contains(slotId) && TileRules.isFree(slotId, assigned)) {
                    tiles.add(slotId);
                }
            });

            if (tiles.size() < 2) {
                return null;
            }

            Collections.shuffle(tiles, random);

            String slot1 = tiles.get(0);
            String slot2 = tiles.get(1);
            TileFace face = facePool.get(poolIndex++);

            assigned.add(slot1);
            assigned.add(slot2);
            order.add(
                    GeneratedPair.builder()
                            .slot1(slot1)
                            .slot2(slot2)
                            .face(face)
                            .build()
            );
        }

        return order;
    }

    private List<TileFace> shuffledFacePool(Random random) {

        List<TileFace> faces = new ArrayList<>(36);

        for (int value = 1; value <= 9; value++) {
            faces.add(new TileFace(TileSuit.BAMBOO, value));
        }

        for (int value = 1; value <= 9; value++) {
            faces.add(new TileFace(TileSuit.CHARACTERS, value));
        }

        for (int value = 1; value <= 9; value++) {
            faces.add(new TileFace(TileSuit.CIRCLES, value));
        }

        for (int value = 1; value <= 4; value++) {
            faces.add(new TileFace(TileSuit.WIND, value));
        }

        for (int value = 1; value <= 3; value++) {
            faces.add(new TileFace(TileSuit.DRAGON, value));
        }

        faces.add(new TileFace(TileSuit.FLOWER, 1));
        faces.add(new TileFace(TileSuit.SEASON, 1));

        Collections.shuffle(faces, random);

        return faces;
    }
}
