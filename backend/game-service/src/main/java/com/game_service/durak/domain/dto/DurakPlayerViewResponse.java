package com.game_service.durak.domain.dto;

import com.game_service.durak.domain.entity.DurakCard;
import com.game_service.durak.domain.entity.DurakTablePair;
import com.game_service.durak.domain.enums.DurakAction;
import com.game_service.durak.domain.enums.DurakCardSuit;
import com.game_service.durak.domain.enums.DurakPhase;
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
