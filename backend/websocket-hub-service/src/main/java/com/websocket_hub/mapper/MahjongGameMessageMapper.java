package com.websocket_hub.mapper;

import com.websocket_hub.domain.dto.client.MahjongGameInternalRequest;
import com.websocket_hub.domain.dto.client.MahjongGameInternalResponse;
import com.websocket_hub.domain.dto.message.MahjongGameMessage;
import com.websocket_hub.domain.entity.MahjongBoard;
import com.websocket_hub.domain.enums.MessageType;
import com.websocket_hub.domain.enums.events.MahjongGameEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface MahjongGameMessageMapper extends MessageMapper {

    @Mapping(target = "playerGuid", ignore = true)
    @Mapping(target = "slot1", ignore = true)
    @Mapping(target = "slot2", ignore = true)
    MahjongGameInternalRequest toStartRequest(UUID roomId, List<UUID> players);

    @Mapping(target = "players", ignore = true)
    MahjongGameInternalRequest toMoveRequest(UUID roomId, UUID playerGuid, String slot1, String slot2);

    @Mapping(target = "winner", ignore = true)
    @Mapping(target = "tilesRemaining", ignore = true)
    @Mapping(target = "slot2", ignore = true)
    @Mapping(target = "slot1", ignore = true)
    @Mapping(target = "seconds", ignore = true)
    @Mapping(target = "removedSlotIds", ignore = true)
    @Mapping(target = "message", ignore = true)
    @Mapping(target = "fromUserId", ignore = true)
    @Mapping(target = "bet", ignore = true)
    @Mapping(target = "availableMoves", ignore = true)
    MahjongGameMessage toStartMessage(MahjongBoard board,
                                      long seed,
                                      MessageType type,
                                      MahjongGameEvent event,
                                      UUID toUserId,
                                      UUID roomId,
                                      Map<UUID, String> players);

    @Mapping(target = "winner", ignore = true)
    @Mapping(target = "tiles", ignore = true)
    @Mapping(target = "slot2", ignore = true)
    @Mapping(target = "slot1", ignore = true)
    @Mapping(target = "seconds", ignore = true)
    @Mapping(target = "players", ignore = true)
    @Mapping(target = "message", ignore = true)
    @Mapping(target = "fromUserId", ignore = true)
    @Mapping(target = "bet", ignore = true)
    MahjongGameMessage toMoverStateMessage(MahjongGameInternalResponse response,
                                           MessageType type,
                                           MahjongGameEvent event,
                                           UUID toUserId,
                                           UUID roomId);

    @Mapping(target = "winner", ignore = true)
    @Mapping(target = "tiles", ignore = true)
    @Mapping(target = "slot2", ignore = true)
    @Mapping(target = "slot1", ignore = true)
    @Mapping(target = "seconds", ignore = true)
    @Mapping(target = "players", ignore = true)
    @Mapping(target = "message", ignore = true)
    @Mapping(target = "fromUserId", ignore = true)
    @Mapping(target = "bet", ignore = true)
    @Mapping(target = "removedSlotIds", ignore = true)
    @Mapping(target = "availableMoves", ignore = true)
    @Mapping(target = "tilesRemaining", source = "response.tilesRemaining")
    MahjongGameMessage toOpponentStateMessage(MahjongGameInternalResponse response,
                                              MessageType type,
                                              MahjongGameEvent event,
                                              UUID toUserId,
                                              UUID roomId);

    @Mapping(target = "toUserId", ignore = true)
    @Mapping(target = "tilesRemaining", ignore = true)
    @Mapping(target = "tiles", ignore = true)
    @Mapping(target = "slot2", ignore = true)
    @Mapping(target = "slot1", ignore = true)
    @Mapping(target = "seed", ignore = true)
    @Mapping(target = "seconds", ignore = true)
    @Mapping(target = "removedSlotIds", ignore = true)
    @Mapping(target = "players", ignore = true)
    @Mapping(target = "message", ignore = true)
    @Mapping(target = "fromUserId", ignore = true)
    @Mapping(target = "bet", ignore = true)
    @Mapping(target = "availableMoves", ignore = true)
    MahjongGameMessage toGameOverMessage(MessageType type,
                                         MahjongGameEvent event,
                                         UUID roomId,
                                         UUID winner);

    @Mapping(target = "winner", ignore = true)
    @Mapping(target = "tilesRemaining", ignore = true)
    @Mapping(target = "tiles", ignore = true)
    @Mapping(target = "slot2", ignore = true)
    @Mapping(target = "slot1", ignore = true)
    @Mapping(target = "seed", ignore = true)
    @Mapping(target = "removedSlotIds", ignore = true)
    @Mapping(target = "players", ignore = true)
    @Mapping(target = "message", ignore = true)
    @Mapping(target = "fromUserId", ignore = true)
    @Mapping(target = "bet", ignore = true)
    @Mapping(target = "availableMoves", ignore = true)
    MahjongGameMessage toDeadlockWaitMessage(MessageType type,
                                             MahjongGameEvent event,
                                             UUID toUserId,
                                             UUID roomId,
                                             int seconds);

    @Mapping(target = "players", ignore = true)
    @Mapping(target = "playerGuid", ignore = true)
    @Mapping(target = "slot1", ignore = true)
    @Mapping(target = "slot2", ignore = true)
    MahjongGameInternalRequest toFinishRequest(UUID roomId, UUID winnerId, Map<UUID, Integer> tilesCleared);
}
