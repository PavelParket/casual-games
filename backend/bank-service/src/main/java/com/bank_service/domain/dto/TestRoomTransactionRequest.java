package com.bank_service.domain.dto;

import com.bank_service.domain.enums.RoomType;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
public record TestRoomTransactionRequest(
        @NotNull(message = "Room ID cannot be null")
        UUID roomId
) implements GameTransactionRequest {

    @Override
    public RoomType roomType() {
        return RoomType.ROOM_TEST;
    }
}
