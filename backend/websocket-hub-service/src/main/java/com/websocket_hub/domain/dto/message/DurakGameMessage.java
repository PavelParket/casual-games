package com.websocket_hub.domain.dto.message;

import com.websocket_hub.domain.entity.DurakCard;
import com.websocket_hub.domain.entity.DurakTablePair;
import com.websocket_hub.domain.enums.MessageType;
import com.websocket_hub.domain.enums.events.DurakGameEvent;
import com.websocket_hub.domain.enums.model.DurakAction;
import com.websocket_hub.domain.enums.model.DurakCardSuit;
import com.websocket_hub.domain.enums.model.DurakPhase;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Builder
public record DurakGameMessage(

        MessageType type,

        DurakGameEvent event,

        UUID fromUserId,

        UUID toUserId,

        UUID roomId,

        String message,

        DurakAction action,

        DurakCard card,

        BigDecimal bet,

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

        UUID defenderId,

        UUID winnerId,

        Integer remainingSeconds

) implements Message<DurakGameEvent> {
}
