package com.game_service.mahjong.util;

import com.game_service.common.exception.GameInternalException;
import com.game_service.mahjong.domain.entity.FacePair;
import com.game_service.mahjong.domain.entity.GeneratedPair;
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

        List<FacePair> facePool = shuffledFacePool(random);
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
            FacePair faceTemplate = facePool.get(poolIndex++);

            assigned.add(slot1);
            assigned.add(slot2);
            order.add(
                    GeneratedPair.builder()
                            .slot1(slot1)
                            .slot2(slot2)
                            .face1(faceTemplate.getFace1())
                            .face2(faceTemplate.getFace2())
                            .build()
            );
        }

        return order;
    }

    private List<FacePair> shuffledFacePool(Random random) {

        List<FacePair> pairs = new ArrayList<>(72);

        addExactQuad(pairs, TileSuit.BAMBOO, 1, 9);
        addExactQuad(pairs, TileSuit.CHARACTERS, 1, 9);
        addExactQuad(pairs, TileSuit.CIRCLES, 1, 9);
        addExactQuad(pairs, TileSuit.WIND, 1, 4);
        addExactQuad(pairs, TileSuit.DRAGON, 1, 3);

        pairs.add(new FacePair(new TileFace(TileSuit.FLOWER, 1), new TileFace(TileSuit.FLOWER, 2)));
        pairs.add(new FacePair(new TileFace(TileSuit.FLOWER, 3), new TileFace(TileSuit.FLOWER, 4)));
        pairs.add(new FacePair(new TileFace(TileSuit.SEASON, 1), new TileFace(TileSuit.SEASON, 2)));
        pairs.add(new FacePair(new TileFace(TileSuit.SEASON, 3), new TileFace(TileSuit.SEASON, 4)));

        Collections.shuffle(pairs, random);

        return pairs;
    }

    private void addExactQuad(List<FacePair> pairs, TileSuit suit, int fromValue, int toValue) {
        for (int value = fromValue; value <= toValue; value++) {
            TileFace face = new TileFace(suit, value);
            pairs.add(new FacePair(face, face));
            pairs.add(new FacePair(face, face));
        }
    }
}
