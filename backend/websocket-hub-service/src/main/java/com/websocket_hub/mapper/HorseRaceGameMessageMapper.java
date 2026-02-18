package com.websocket_hub.mapper;

import com.websocket_hub.domain.dto.message.HorseRaceMessage;
import com.websocket_hub.domain.enums.MessageType;
import com.websocket_hub.domain.enums.events.HorseRaceEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Map;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface HorseRaceGameMessageMapper extends MessageMapper {

    @Mapping(target = "fromUserId", ignore = true)
    @Mapping(target = "toUserId", ignore = true)
    @Mapping(target = "message", ignore = true)
    @Mapping(target = "horseCount", ignore = true)
    @Mapping(target = "odds", ignore = true)
    @Mapping(target = "seedHash", ignore = true)
    @Mapping(target = "serverSeed", ignore = true)
    @Mapping(target = "winnerHorseIndex", ignore = true)
    @Mapping(target = "segmentsCount", ignore = true)
    @Mapping(target = "ticks", ignore = true)
    @Mapping(target = "participants", ignore = true)
    HorseRaceMessage toCreateRequest(MessageType type, HorseRaceEvent event, UUID roomId);

    @Mapping(target = "fromUserId", ignore = true)
    @Mapping(target = "toUserId", ignore = true)
    @Mapping(target = "message", ignore = true)
    @Mapping(target = "odds", ignore = true)
    @Mapping(target = "seedHash", ignore = true)
    @Mapping(target = "serverSeed", ignore = true)
    @Mapping(target = "winnerHorseIndex", ignore = true)
    @Mapping(target = "segmentsCount", ignore = true)
    @Mapping(target = "ticks", ignore = true)
    HorseRaceMessage toStartRequest(MessageType type,
                                    HorseRaceEvent event,
                                    UUID roomId,
                                    Map<UUID, String> participants,
                                    Integer horseCount);

    @Mapping(target = "horseCount", ignore = true)
    @Mapping(target = "odds", ignore = true)
    @Mapping(target = "seedHash", ignore = true)
    @Mapping(target = "serverSeed", ignore = true)
    @Mapping(target = "winnerHorseIndex", ignore = true)
    @Mapping(target = "segmentsCount", ignore = true)
    @Mapping(target = "ticks", ignore = true)
    @Mapping(target = "participants", ignore = true)
    HorseRaceMessage toFinishRequest(MessageType type,
                                     HorseRaceEvent event,
                                     UUID fromUserId,
                                     UUID toUserId,
                                     UUID roomId,
                                     String message);
}
