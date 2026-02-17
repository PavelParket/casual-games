package com.game_service.horse_race.util;

import com.game_service.horse_race.domain.entity.HorseTick;
import lombok.experimental.UtilityClass;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

@UtilityClass
public class HorseRaceGameUtils {

    public final int MIN_HORSES = 3;

    public final int MAX_HORSES = 8;

    public final int MIN_SEGMENTS = 6;

    public final int MAX_SEGMENTS = 10;

    public final int MIN_SPEED = 1;

    public final int MAX_SPEED = 10;

    public double[] calculateOdds(int horseCount) {
        return IntStream.range(0, horseCount)
                .mapToDouble(horse -> horse + 1.0)
                .toArray();
    }

    public int calculateHorseCount() {
        return HorseRaceGameUtils.MIN_HORSES
                + ThreadLocalRandom.current()
                .nextInt(HorseRaceGameUtils.MAX_HORSES - HorseRaceGameUtils.MIN_HORSES + 1);
    }

    public int calculateSegmentsCount() {
        return HorseRaceGameUtils.MIN_SEGMENTS
                + ThreadLocalRandom.current()
                .nextInt(HorseRaceGameUtils.MAX_SEGMENTS - HorseRaceGameUtils.MIN_SEGMENTS + 1);
    }

    public int[][] buildSpeeds(Random seededRandom, int horseCount, int segmentsCount) {
        int speedRange = MAX_SPEED - MIN_SPEED + 1;
        int[][] speeds = new int[horseCount][segmentsCount];

        for (int horse = 0; horse < horseCount; horse++) {
            for (int segment = 0; segment < segmentsCount; segment++) {
                speeds[horse][segment] = MIN_SPEED + seededRandom.nextInt(speedRange);
            }
        }

        return speeds;
    }

    public double[] buildTotalDistances(int[][] speeds, int horseCount, int segmentsCount) {
        double[] totals = new double[horseCount];

        for (int horse = 0; horse < horseCount; horse++) {
            for (int segment = 0; segment < segmentsCount; segment++) {
                totals[horse] += speeds[horse][segment];
            }
        }

        return totals;
    }

    public int findWinner(double[] totalDistances, int horseCount) {
        int winner = 0;

        for (int horse = 1; horse < horseCount; horse++) {
            if (totalDistances[horse] > totalDistances[winner]) {
                winner = horse;
            }
        }

        return winner;
    }

    public List<HorseTick> buildTicks(int[][] speeds, double[] totalDistances, int horseCount, int segmentsCount) {
        double maxDistance = 0;
        for (double d : totalDistances) {
            if (d > maxDistance)
                maxDistance = d;
        }

        double[] cumulative = new double[horseCount];
        List<HorseTick> ticks = new ArrayList<>(segmentsCount);

        for (int tick = 0; tick < segmentsCount; tick++) {
            double[] positions = new double[horseCount];

            for (int horse = 0; horse < horseCount; horse++) {
                cumulative[horse] += speeds[horse][tick];
                positions[horse] = (cumulative[horse] / maxDistance) * 100.0;
            }

            ticks.add(HorseTick.builder()
                    .tickIndex(tick)
                    .positions(positions)
                    .build());
        }

        return ticks;
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
