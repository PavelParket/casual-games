package com.game_service.horse_race.mapper;

import com.game_service.common.enums.MessageType;
import com.game_service.horse_race.domain.dto.HorseRacePresetResponse;
import com.game_service.horse_race.domain.dto.HorseRaceResponse;
import com.game_service.horse_race.domain.entity.HorseRace;
import com.game_service.horse_race.domain.entity.HorseTick;
import com.game_service.horse_race.domain.enums.HorseRaceEvent;
import com.game_service.horse_race.domain.enums.HorseRaceStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface HorseRaceMapper {

    @Mapping(target = "event", ignore = true)
    HorseRacePresetResponse toPresetResponse(MessageType type,
                                             UUID roomId,
                                             String message,
                                             int horseCount,
                                             double[] odds);

    @Mapping(target = "fromUserId", ignore = true)
    @Mapping(target = "toUserId", ignore = true)
    HorseRaceResponse toResponse(MessageType type,
                                 HorseRaceEvent event,
                                 UUID roomId,
                                 String message,
                                 String seedHash,
                                 String serverSeed,
                                 Integer horseCount,
                                 double[] odds,
                                 Integer winnerHorseIndex,
                                 Integer segmentsCount,
                                 List<HorseTick> ticks);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    HorseRace toEntity(UUID roomId,
                       String serverSeed,
                       String seedHash,
                       Integer horseCount,
                       Integer winnerHorseIndex,
                       Integer segmentsCount,
                       HorseRaceStatus status);
}
