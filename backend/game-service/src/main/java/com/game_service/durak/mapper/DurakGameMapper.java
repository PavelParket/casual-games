package com.game_service.durak.mapper;

import com.game_service.common.dto.GameMatchResponse;
import com.game_service.common.enums.GameResult;
import com.game_service.common.enums.GameType;
import com.game_service.durak.domain.dto.DurakGameResponse;
import com.game_service.durak.domain.dto.DurakPlayerViewResponse;
import com.game_service.durak.domain.entity.Durak;
import com.game_service.durak.domain.entity.DurakCard;
import com.game_service.durak.domain.enums.DurakAction;
import com.game_service.durak.domain.enums.DurakPhase;
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

    GameMatchResponse toMatchResponse(Durak durak, UUID userGuid, GameType gameType, GameResult gameResult);
}
