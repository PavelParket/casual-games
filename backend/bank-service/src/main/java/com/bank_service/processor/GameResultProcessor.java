package com.bank_service.processor;

import com.bank_service.domain.dto.GameTransactionRequest;
import com.bank_service.domain.dto.ProcessingResult;
import com.bank_service.domain.enums.RoomType;

public interface GameResultProcessor {

    boolean supports(RoomType roomType);

    RoomType getRoomType();

    ProcessingResult process(GameTransactionRequest request);
}
