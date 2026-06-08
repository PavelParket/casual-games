package com.game_service.horse_race.factory;

import com.game_service.horse_race.domain.entity.HorseRace;
import com.game_service.horse_race.domain.enums.HorseRaceStatus;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class HorseRaceFactory {

    public HorseRace create(UUID roomId,
                            String serverSeed,
                            String seedHash,
                            Integer horseCount,
                            Integer winnerHorseIndex,
                            Integer segmentsCount,
                            Map<UUID, Integer> players) {
        return HorseRace.builder()
                .roomId(roomId)
                .serverSeed(serverSeed)
                .seedHash(seedHash)
                .horseCount(horseCount)
                .winnerHorseIndex(winnerHorseIndex)
                .segmentsCount(segmentsCount)
                .status(HorseRaceStatus.RUNNING)
                .players(players)
                .build();
    }
}
