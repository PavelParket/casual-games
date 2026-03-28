package com.websocket_hub.domain.dto.client;

import com.websocket_hub.domain.entity.DurakCard;
import com.websocket_hub.domain.entity.DurakTablePair;
import com.websocket_hub.domain.enums.model.DurakAction;
import com.websocket_hub.domain.enums.model.DurakCardSuit;
import com.websocket_hub.domain.enums.model.DurakPhase;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record DurakPlayerViewResponse(

        Long gameId,

        UUID playerGuid,

        DurakPhase phase,

        List<DurakCard> myCards,

        Integer opponentCardCount,

        Integer deckCardsLeft,

        DurakCard trumpCard,

        DurakCardSuit trumpSuit,

        List<DurakTablePair> table,

        Boolean isMyTurn,

        List<DurakAction> availableActions,

        UUID attackerId,

        UUID defenderId
) {
}
