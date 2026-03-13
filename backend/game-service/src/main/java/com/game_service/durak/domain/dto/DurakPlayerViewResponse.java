package com.game_service.durak.domain.dto;

import com.game_service.durak.domain.entity.Card;
import com.game_service.durak.domain.entity.TablePair;
import com.game_service.durak.domain.enums.CardSuit;
import com.game_service.durak.domain.enums.DurakAction;
import com.game_service.durak.domain.enums.DurakEvent;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record DurakPlayerViewResponse(

        Long gameId,

        UUID playerGuid,

        DurakEvent phase,

        List<Card> myCards,

        Integer opponentCardCount,

        Integer deckCardsLeft,

        Card trumpCard,

        CardSuit trumpSuit,

        List<TablePair> table,

        Boolean isMyTurn,

        List<DurakAction> availableActions,

        UUID attackerId,

        UUID defenderId
) {
}
