package com.casualgames.gameservice.durak.domain.dto;

import com.casualgames.gameservice.durak.domain.entity.DurakCard;
import com.casualgames.gameservice.durak.domain.entity.DurakTablePair;
import com.casualgames.gameservice.durak.domain.enums.DurakAction;
import com.casualgames.gameservice.durak.domain.enums.DurakCardSuit;
import com.casualgames.gameservice.durak.domain.enums.DurakPhase;
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
