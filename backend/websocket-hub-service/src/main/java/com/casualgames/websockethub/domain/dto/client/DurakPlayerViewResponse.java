package com.casualgames.websockethub.domain.dto.client;

import com.casualgames.websockethub.domain.entity.DurakCard;
import com.casualgames.websockethub.domain.entity.DurakTablePair;
import com.casualgames.websockethub.domain.enums.model.DurakAction;
import com.casualgames.websockethub.domain.enums.model.DurakCardSuit;
import com.casualgames.websockethub.domain.enums.model.DurakPhase;
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
