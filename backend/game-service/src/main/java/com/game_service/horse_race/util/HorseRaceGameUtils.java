package com.game_service.horse_race.util;

import com.game_service.horse_race.domain.entity.HorseRaceHorseKeyframes;
import com.game_service.horse_race.domain.entity.HorseRaceKeyframe;
import lombok.experimental.UtilityClass;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

@UtilityClass
public class HorseRaceGameUtils {

    public static final int MIN_HORSES = 3;
    public static final int MAX_HORSES = 6;
    public static final int MIN_SPEED = 10;
    public static final int MAX_SPEED = 13;
    public static final int SEGMENTS = 20;

    public final int ONE = 1;

    public List<Double> calculateOdds(Integer horseCount) {
        return IntStream.range(0, horseCount)
                .mapToDouble(horse -> horse + 1.0)
                .boxed()
                .toList();
    }

    public Integer calculateHorseCount() {
        return MIN_HORSES + ThreadLocalRandom.current().nextInt(MAX_HORSES - MIN_HORSES + ONE);
    }

    public Integer[][] buildSpeeds(Random seededRandom, Integer horseCount, Integer segmentsCount) {
        int speedRange = MAX_SPEED - MIN_SPEED + ONE;
        Integer[][] speeds = new Integer[horseCount][segmentsCount];

        for (int horse = 0; horse < horseCount; horse++) {
            for (int segment = 0; segment < segmentsCount; segment++) {
                speeds[horse][segment] = MIN_SPEED + seededRandom.nextInt(speedRange);
            }
        }

        return speeds;
    }

    public List<Double> buildTotalDistances(Integer[][] speeds, Integer horseCount, Integer segmentsCount) {
        List<Double> totals = new ArrayList<>(Collections.nCopies(horseCount, 0.0));

        for (int horse = 0; horse < horseCount; horse++) {
            for (int segment = 0; segment < segmentsCount; segment++) {
                totals.set(horse, totals.get(horse) + speeds[horse][segment]);
            }
        }

        return totals;
    }

    public Integer findWinner(List<Double> totalDistances, Integer horseCount) {
        int winner = 0;

        for (int horse = 1; horse < horseCount; horse++) {
            if (totalDistances.get(horse) > totalDistances.get(winner)) {
                winner = horse;
            }
        }

        return winner;
    }

    public List<HorseRaceHorseKeyframes> buildKeyFrames(Integer[][] speeds,
                                                        List<Double> totalDistances,
                                                        Integer horseCount,
                                                        Integer segmentsCount) {
        double maxDistance = totalDistances.stream()
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(1.0);

        List<HorseRaceHorseKeyframes> result = new ArrayList<>(horseCount);

        for (int horse = 0; horse < horseCount; horse++) {
            List<HorseRaceKeyframe> keyframes = new ArrayList<>(segmentsCount + ONE);

            keyframes.add(HorseRaceKeyframe.builder()
                    .offset(0.0)
                    .position(0.0)
                    .build());

            double cumulative = 0.0;

            for (int segment = 0; segment < segmentsCount; segment++) {
                cumulative += speeds[horse][segment];
                double position = (cumulative / maxDistance) * 100;
                double offset = (double) (segment + ONE) / segmentsCount;

                keyframes.add(HorseRaceKeyframe.builder()
                        .offset(offset)
                        .position(position)
                        .build());
            }

            result.add(HorseRaceHorseKeyframes.builder()
                    .horseIndex(horse)
                    .keyframes(keyframes)
                    .build());
        }

        return result;
    }

    public String calculateHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }

            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
