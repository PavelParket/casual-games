package com.bank_service.domain.dto.game;

import com.bank_service.domain.entity.PlayerBet;
import com.bank_service.domain.enums.RoomType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record DurakTransactionRequest(

        @NotNull(message = "Room ID cannot be null")
        UUID roomId,

        @NotEmpty(message = "Player bets cannot be empty")
        @Size(min = 2, max = 2, message = "Durak requires exactly 2 players")
        @Valid
        List<PlayerBet> playerBets,

        UUID winner

) implements GameTransactionRequest {

    @Override
    public RoomType roomType() {
        return RoomType.DURAK;
    }

    public boolean isDraw() {
        return winner == null;
    }
}
