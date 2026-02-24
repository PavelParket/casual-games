package com.bank_service.domain.dto;

import com.bank_service.domain.entity.HorseRacePlayerBet;
import com.bank_service.domain.enums.RoomType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record HorseRaceTransactionRequest(
        @NotNull(message = "Room ID cannot be null")
        UUID roomId,

        @NotNull(message = "Winner horse index cannot be null")
        Integer winnerHorseIndex,

        @NotEmpty(message = "Player bets cannot be empty")
        @Valid
        List<HorseRacePlayerBet> playerBets
) implements GameTransactionRequest {

    @Override
    public RoomType roomType() {
        return RoomType.HORSE_RACE;
    }
}
