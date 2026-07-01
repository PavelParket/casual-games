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

    default MahjongGameMessage toStartMessage(MahjongBoard board,
                                              long seed,
                                              MessageType type,
                                              MahjongGameEvent event,
                                              UUID toUserId,
                                              UUID roomId,
                                              Map<UUID, String> players) {
        return MahjongGameMessage.builder()
                .type(type)
                .event(event)
                .toUserId(toUserId)
                .roomId(roomId)
                .seed(seed)
                .tiles(board.getTiles())
                .players(players)
                .build();
    }

    default MahjongGameMessage toMoverStateMessage(MahjongGameInternalResponse response,
                                                   MessageType type,
                                                   MahjongGameEvent event,
                                                   UUID toUserId,
                                                   UUID roomId) {
        return MahjongGameMessage.builder()
                .type(type)
                .event(event)
                .toUserId(toUserId)
                .roomId(roomId)
                .removedSlotIds(response.removedSlotIds())
                .tilesRemaining(response.tilesRemaining())
                .availableMoves(response.availableMoves())
                .build();
    }

    default MahjongGameMessage toOpponentStateMessage(MahjongGameInternalResponse response,
                                                      MessageType type,
                                                      MahjongGameEvent event,
                                                      UUID toUserId,
                                                      UUID roomId) {
        return MahjongGameMessage.builder()
                .type(type)
                .event(event)
                .toUserId(toUserId)
                .roomId(roomId)
                .tilesRemaining(response.opponentTilesRemaining())
                .build();
    }

    default MahjongGameMessage toGameOverMessage(MessageType type,
                                                 MahjongGameEvent event,
                                                 UUID roomId,
                                                 UUID winner) {
        return MahjongGameMessage.builder()
                .type(type)
                .event(event)
                .roomId(roomId)
                .winner(winner)
                .build();
    }
}
