package com.game_service.horse_race.util;

import com.game_service.horse_race.domain.entity.HorseRaceTick;
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

    public final int MIN_HORSES = 3;
    public final int MAX_HORSES = 6;
    public final int MIN_SPEED = 10;
    public final int MAX_SPEED = 11;
    public final int SEGMENTS = 20;

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

    public List<HorseRaceTick> buildTicks(Integer[][] speeds,
                                          List<Double> totalDistances,
                                          Integer horseCount,
                                          Integer segmentsCount) {
        double maxDistance = totalDistances.stream()
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(1.0);

        double[] cumulative = new double[horseCount];
        List<HorseRaceTick> ticks = new ArrayList<>(segmentsCount + ONE);

        double[] zeroPositions = new double[horseCount];
        ticks.add(HorseRaceTick.builder()
                .tickIndex(0)
                .positions(zeroPositions)
                .build());

        for (int tick = 0; tick < segmentsCount; tick++) {
            double[] positions = new double[horseCount];

            for (int horse = 0; horse < horseCount; horse++) {
                cumulative[horse] += speeds[horse][tick];
                positions[horse] = (cumulative[horse] / maxDistance) * 100.0;
            }

            ticks.add(HorseRaceTick.builder()
                    .tickIndex(tick + ONE)
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
