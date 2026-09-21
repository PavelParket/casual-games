package com.casualgames.websockethub.mapper;

import com.casualgames.websockethub.domain.dto.client.HorseRaceGameInternalRequest;
import com.casualgames.websockethub.domain.dto.client.HorseRaceGameInternalResponse;
import com.casualgames.websockethub.domain.dto.message.HorseRaceGameMessage;
import com.casualgames.websockethub.domain.entity.HorseRaceGamePreset;
import com.casualgames.websockethub.domain.enums.MessageType;
import com.casualgames.websockethub.domain.enums.events.HorseRaceEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Map;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface HorseRaceGameMessageMapper extends MessageMapper {

    HorseRaceGameInternalRequest toStartRequest(HorseRaceEvent event,
                                                UUID roomId,
                                                Map<UUID, Integer> participants,
                                                Integer horseCount);

    @Mapping(target = "participants", ignore = true)
    @Mapping(target = "horseCount", ignore = true)
    HorseRaceGameInternalRequest toFinishRequest(HorseRaceEvent event,
                                                 UUID roomId);

    HorseRaceGamePreset toPreset(HorseRaceGameInternalResponse createResponse);

    @Mapping(target = "horseIndex", ignore = true)
    @Mapping(target = "bet", ignore = true)
    @Mapping(target = "remainingSeconds", ignore = true)
    HorseRaceGameMessage toMessage(HorseRaceGameInternalResponse horseRaceGameInternalResponse,
                                   MessageType type,
                                   HorseRaceEvent event,
                                   UUID fromUserId,
                                   UUID toUserId,
                                   String message,
                                   Map<UUID, String> participants);

    @Mapping(target = "message", ignore = true)
    @Mapping(target = "participants", ignore = true)
    @Mapping(target = "horseCount", ignore = true)
    @Mapping(target = "odds", ignore = true)
    @Mapping(target = "seedHash", ignore = true)
    @Mapping(target = "serverSeed", ignore = true)
    @Mapping(target = "winnerHorseIndex", ignore = true)
    @Mapping(target = "segmentsCount", ignore = true)
    @Mapping(target = "horseKeyframes", ignore = true)
    @Mapping(target = "horseIndex", ignore = true)
    @Mapping(target = "bet", ignore = true)
    HorseRaceGameMessage toCountdownMessage(MessageType type,
                                            HorseRaceEvent event,
                                            UUID fromUserId,
                                            UUID toUserId,
                                            UUID roomId,
                                            Integer remainingSeconds);
}
