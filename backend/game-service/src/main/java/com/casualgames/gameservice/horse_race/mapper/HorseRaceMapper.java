package com.casualgames.gameservice.horse_race.mapper;

import com.casualgames.gameservice.common.dto.HorseRaceGameMatchResponse;
import com.casualgames.gameservice.common.enums.GameResult;
import com.casualgames.gameservice.common.enums.GameType;
import com.casualgames.gameservice.horse_race.domain.dto.HorseRaceGamePresetResponse;
import com.casualgames.gameservice.horse_race.domain.dto.HorseRaceGameResponse;
import com.casualgames.gameservice.horse_race.domain.entity.HorseRace;
import com.casualgames.gameservice.horse_race.domain.entity.HorseRaceHorseKeyframes;
import com.casualgames.gameservice.horse_race.domain.enums.HorseRaceEvent;
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
