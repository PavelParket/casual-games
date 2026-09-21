package com.casualgames.gameservice.common.service.provider;

import com.casualgames.gameservice.common.dto.GameMatchRequestFilter;
import com.casualgames.gameservice.common.dto.GameMatchResponse;
import com.casualgames.gameservice.common.enums.GameResult;
import com.casualgames.gameservice.common.enums.GameType;
import com.casualgames.gameservice.horse_race.domain.entity.HorseRace;
import com.casualgames.gameservice.horse_race.mapper.HorseRaceMapper;
import com.casualgames.gameservice.horse_race.repository.HorseRaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class HorseRaceMatchesProvider implements GameMatchesProvider {

    private final HorseRaceRepository horseRaceRepository;

    private final HorseRaceMapper horseRaceMapper;

    @Override
    public GameType gameType() {
        return GameType.HORSE_RACE;
    }

    @Override
    public Page<GameMatchResponse> findMatches(UUID userGuid, GameMatchRequestFilter gameMatchRequestFilter, Pageable pageable) {
        Page<HorseRace> page = horseRaceRepository.findMatchHistory(userGuid, gameMatchRequestFilter.isWinner(), pageable);

        return page.map(horseRace ->
                horseRaceMapper.toMatchResponse(
                        horseRace,
                        GameType.HORSE_RACE,
                        resolveGameResult(horseRace, userGuid)
                )
        );
    }

    private GameResult resolveGameResult(HorseRace horseRace, UUID userGuid) {
        Integer playerHorseIndex = horseRace.getPlayers().get(userGuid);

        if (playerHorseIndex == null) {
            return GameResult.NO_DATA;
        }

        return playerHorseIndex.equals(horseRace.getWinnerHorseIndex()) ? GameResult.WIN : GameResult.LOSS;
    }
}
