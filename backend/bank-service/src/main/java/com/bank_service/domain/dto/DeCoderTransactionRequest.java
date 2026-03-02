package com.bank_service.domain.dto;

import com.bank_service.domain.entity.PlayerBet;
import com.bank_service.domain.enums.RoomType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
public record DeCoderTransactionRequest(
        @NotNull(message = "Room ID cannot be null")
        UUID roomId,

        @NotNull(message = "Player bet info cannot be null")
        @Valid
        PlayerBet playerBet,

        UUID winner
) implements GameTransactionRequest {

    @Override
    public RoomType roomType() {
        return RoomType.DE_CODER;
    }

    public boolean isWin() {
        return winner != null;
    }
}