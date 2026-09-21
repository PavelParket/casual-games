package com.casualgames.gameservice.durak.mapper;

import com.casualgames.gameservice.common.dto.DurakGameMatchResponse;
import com.casualgames.gameservice.common.enums.GameResult;
import com.casualgames.gameservice.common.enums.GameType;
import com.casualgames.gameservice.durak.domain.dto.DurakGameResponse;
import com.casualgames.gameservice.durak.domain.dto.DurakPlayerViewResponse;
import com.casualgames.gameservice.durak.domain.entity.Durak;
import com.casualgames.gameservice.durak.domain.entity.DurakCard;
import com.casualgames.gameservice.durak.domain.enums.DurakAction;
import com.casualgames.gameservice.durak.domain.enums.DurakPhase;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface DurakGameMapper {

    @Mapping(target = "isGameOver", source = "game.phase", qualifiedByName = "phaseToIsGameOver")
    DurakGameResponse toResponse(Durak game, List<DurakPlayerViewResponse> playerViews);

    @Mapping(target = "gameId", source = "game.id")
    @Mapping(target = "deckCardsLeft", source = "game.deck", qualifiedByName = "getDeckSize")
    DurakPlayerViewResponse toPlayerView(Durak game,
                                         UUID playerGuid,
                                         List<DurakCard> myCards,
                                         Integer opponentCardCount,
                                         Boolean isMyTurn,
                                         List<DurakAction> availableActions);

    @Named("phaseToIsGameOver")
    default Boolean phaseToIsGameOver(DurakPhase phase) {
        return DurakPhase.GAME_OVER.equals(phase);
    }

    @Named("getDeckSize")
    default Integer getDeckSize(List<DurakCard> deck) {
        return deck == null ? 0 : deck.size();
    }

    DurakGameMatchResponse toMatchResponse(Durak durak, UUID userGuid, GameType gameType, GameResult gameResult);
}
