package com.game_service.horse_race.mapper;

import com.game_service.common.dto.HorseRaceGameMatchResponse;
import com.game_service.common.enums.GameResult;
import com.game_service.common.enums.GameType;
import com.game_service.horse_race.domain.dto.HorseRaceGamePresetResponse;
import com.game_service.horse_race.domain.dto.HorseRaceGameResponse;
import com.game_service.horse_race.domain.entity.HorseRace;
import com.game_service.horse_race.domain.entity.HorseRaceHorseKeyframes;
import com.game_service.horse_race.domain.enums.HorseRaceEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface HorseRaceMapper {

    HorseRaceGamePresetResponse toPresetResponse(UUID roomId, Integer horseCount, List<Double> odds);

    HorseRaceGameResponse toResponse(HorseRace horseRace,
                                     HorseRaceEvent event,
                                     List<Double> odds,
                                     List<HorseRaceHorseKeyframes> horseKeyframes);

    @Mapping(target = "gameType", source = "gameType")
    @Mapping(target = "gameResult", source = "gameResult")
    HorseRaceGameMatchResponse toMatchResponse(HorseRace horseRace, GameType gameType, GameResult gameResult);
}
