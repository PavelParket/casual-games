package com.bank_service.processor;

import com.bank_service.domain.dto.game.GameTransactionRequest;
import com.bank_service.domain.dto.game.GameTransactionResponse;
import com.bank_service.domain.enums.RoomType;

public interface GameTransactionProcessor {

    int ZERO_TRANSACTIONS = 0;

    boolean supports(RoomType roomType);

    RoomType getRoomType();

    GameTransactionResponse process(GameTransactionRequest request);
}
