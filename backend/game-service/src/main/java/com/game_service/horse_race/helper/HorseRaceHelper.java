package com.game_service.horse_race.helper;

import lombok.experimental.UtilityClass;

import java.util.stream.IntStream;

@UtilityClass
public class HorseRaceHelper {

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
}
