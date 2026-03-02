package com.bank_service.domain.dto;

import com.bank_service.domain.enums.RoomType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "roomType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = TestRoomTransactionRequest.class, name = "ROOM_TEST"),
        @JsonSubTypes.Type(value = TicTacToeTransactionRequest.class, name = "TIC_TAC_TOE"),
        @JsonSubTypes.Type(value = HorseRaceTransactionRequest.class, name = "HORSE_RACE"),
        @JsonSubTypes.Type(value = DeCoderTransactionRequest.class, name = "DE_CODER"),
})
public sealed interface GameTransactionRequest permits
        TestRoomTransactionRequest,
        TicTacToeTransactionRequest,
        HorseRaceTransactionRequest,
        DeCoderTransactionRequest {

    UUID roomId();

    RoomType roomType();
}
