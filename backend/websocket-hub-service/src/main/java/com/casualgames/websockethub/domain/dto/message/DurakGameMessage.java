package com.casualgames.websockethub.domain.dto.message;

import com.casualgames.websockethub.domain.entity.DurakCard;
import com.casualgames.websockethub.domain.entity.DurakTablePair;
import com.casualgames.websockethub.domain.enums.MessageType;
import com.casualgames.websockethub.domain.enums.events.DurakGameEvent;
import com.casualgames.websockethub.domain.enums.model.DurakAction;
import com.casualgames.websockethub.domain.enums.model.DurakCardSuit;
import com.casualgames.websockethub.domain.enums.model.DurakPhase;
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
