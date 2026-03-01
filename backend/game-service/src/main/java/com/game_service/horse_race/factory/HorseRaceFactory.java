package com.game_service.horse_race.factory;

import com.game_service.common.factory.Factory;
import com.game_service.horse_race.domain.entity.HorseRace;
import com.game_service.horse_race.domain.enums.HorseRaceStatus;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class HorseRaceFactory implements Factory<HorseRace> {
    @Override
    public HorseRace create(Object... args) {
        if (args.length < 6
                || !(args[0] instanceof UUID roomId)
                || !(args[1] instanceof String serverSeed)
                || !(args[2] instanceof String seedHash)
                || !(args[3] instanceof Integer horseCount)
                || !(args[4] instanceof Integer winnerHorseIndex)
                || !(args[5] instanceof Integer segmentsCount)
        ) {
            throw new IllegalArgumentException(
                    "Expected arguments: roomId, serverSeed, seedHash, horseCount, winnerHorseIndex, segmentsCount"
            );
        }

        return create(roomId, serverSeed, seedHash, horseCount, winnerHorseIndex, segmentsCount);
    }

    private HorseRace create(UUID roomId,
                             String serverSeed,
                             String seedHash,
                             Integer horseCount,
                             Integer winnerHorseIndex,
                             Integer segmentsCount) {
        return HorseRace.builder()
                .roomId(roomId)
                .serverSeed(serverSeed)
                .seedHash(seedHash)
                .horseCount(horseCount)
                .winnerHorseIndex(winnerHorseIndex)
                .segmentsCount(segmentsCount)
                .status(HorseRaceStatus.RUNNING)
                .build();
    }
}
