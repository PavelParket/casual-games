package com.game_service.durak.mapper;

import com.game_service.durak.domain.dto.DurakGameResponse;
import com.game_service.durak.domain.dto.DurakPlayerViewResponse;
import com.game_service.durak.domain.entity.Card;
import com.game_service.durak.domain.entity.Durak;
import com.game_service.durak.domain.enums.DurakAction;
import com.game_service.durak.domain.enums.DurakEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface DurakGameMapper {

    @Mapping(target = "isGameOver", source = "game.event", qualifiedByName = "eventToIsGameOver")
    DurakGameResponse toResponse(Durak game, List<DurakPlayerViewResponse> playerViews);

    @Mapping(target = "gameId", source = "game.id")
    @Mapping(target = "deckCardsLeft", source = "game.deck", qualifiedByName = "getDeckSize")
    DurakPlayerViewResponse toPlayerView(Durak game,
                                         UUID playerGuid,
                                         List<Card> myCards,
                                         Integer opponentCardCount,
                                         Boolean isMyTurn,
                                         List<DurakAction> availableActions);

    @Named("eventToIsGameOver")
    default Boolean eventToIsGameOver(DurakEvent event) {
        return DurakEvent.GAME_OVER.equals(event);
    }

    @Named("getDeckSize")
    default Integer getDeckSize(List<Card> deck) {
        return deck == null ? 0 : deck.size();
    }
}
