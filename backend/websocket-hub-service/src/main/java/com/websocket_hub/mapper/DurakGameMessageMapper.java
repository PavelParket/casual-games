package com.websocket_hub.mapper;

import com.websocket_hub.domain.dto.client.DurakGameInternalRequest;
import com.websocket_hub.domain.dto.client.DurakPlayerViewResponse;
import com.websocket_hub.domain.dto.message.DurakGameMessage;
import com.websocket_hub.domain.entity.DurakCard;
import com.websocket_hub.domain.enums.MessageType;
import com.websocket_hub.domain.enums.events.DurakGameEvent;
import com.websocket_hub.domain.enums.model.DurakAction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface DurakGameMessageMapper extends MessageMapper {

    @Mapping(target = "fromUserId", ignore = true)
    @Mapping(target = "message", ignore = true)
    @Mapping(target = "action", ignore = true)
    @Mapping(target = "card", ignore = true)
    @Mapping(target = "bet", ignore = true)
    @Mapping(target = "winnerId", ignore = true)
    @Mapping(target = "remainingSeconds", ignore = true)
    DurakGameMessage toGameMessage(MessageType type,
                                   DurakGameEvent event,
                                   UUID toUserId,
                                   UUID roomId,
                                   DurakPlayerViewResponse playerView);

    @Mapping(target = "fromUserId", ignore = true)
    @Mapping(target = "toUserId", ignore = true)
    @Mapping(target = "message", ignore = true)
    @Mapping(target = "action", ignore = true)
    @Mapping(target = "card", ignore = true)
    @Mapping(target = "bet", ignore = true)
    @Mapping(target = "gameId", ignore = true)
    @Mapping(target = "playerGuid", ignore = true)
    @Mapping(target = "phase", ignore = true)
    @Mapping(target = "myCards", ignore = true)
    @Mapping(target = "opponentCardCount", ignore = true)
    @Mapping(target = "deckCardsLeft", ignore = true)
    @Mapping(target = "trumpCard", ignore = true)
    @Mapping(target = "trumpSuit", ignore = true)
    @Mapping(target = "table", ignore = true)
    @Mapping(target = "isMyTurn", ignore = true)
    @Mapping(target = "availableActions", ignore = true)
    @Mapping(target = "attackerId", ignore = true)
    @Mapping(target = "defenderId", ignore = true)
    @Mapping(target = "remainingSeconds", ignore = true)
    DurakGameMessage toGameOverMessage(MessageType type,
                                       DurakGameEvent event,
                                       UUID roomId,
                                       UUID winnerId);

    @Mapping(target = "fromUserId", ignore = true)
    @Mapping(target = "toUserId", ignore = true)
    @Mapping(target = "message", ignore = true)
    @Mapping(target = "action", ignore = true)
    @Mapping(target = "card", ignore = true)
    @Mapping(target = "bet", ignore = true)
    @Mapping(target = "gameId", ignore = true)
    @Mapping(target = "playerGuid", ignore = true)
    @Mapping(target = "phase", ignore = true)
    @Mapping(target = "myCards", ignore = true)
    @Mapping(target = "opponentCardCount", ignore = true)
    @Mapping(target = "deckCardsLeft", ignore = true)
    @Mapping(target = "trumpCard", ignore = true)
    @Mapping(target = "trumpSuit", ignore = true)
    @Mapping(target = "table", ignore = true)
    @Mapping(target = "isMyTurn", ignore = true)
    @Mapping(target = "availableActions", ignore = true)
    @Mapping(target = "attackerId", ignore = true)
    @Mapping(target = "defenderId", ignore = true)
    @Mapping(target = "winnerId", ignore = true)
    DurakGameMessage toTimerMessage(MessageType type,
                                    DurakGameEvent event,
                                    UUID roomId,
                                    Integer remainingSeconds);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "currentActorId", ignore = true)
    @Mapping(target = "action", ignore = true)
    @Mapping(target = "card", ignore = true)
    @Mapping(target = "winnerId", ignore = true)
    DurakGameInternalRequest toStartGameRequest(UUID roomId, List<UUID> players);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "players", ignore = true)
    @Mapping(target = "winnerId", ignore = true)
    DurakGameInternalRequest toMoveGameRequest(UUID roomId,
                                               UUID currentActorId,
                                               DurakAction action,
                                               DurakCard card);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "players", ignore = true)
    @Mapping(target = "currentActorId", ignore = true)
    @Mapping(target = "action", ignore = true)
    @Mapping(target = "card", ignore = true)
    DurakGameInternalRequest toEndGameRequest(UUID roomId, UUID winnerId);
}
