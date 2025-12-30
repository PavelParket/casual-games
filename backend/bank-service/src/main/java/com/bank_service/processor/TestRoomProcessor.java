package com.bank_service.processor;

import com.bank_service.domain.dto.GameTransactionRequest;
import com.bank_service.domain.dto.ProcessingResult;
import com.bank_service.domain.dto.TestRoomTransactionRequest;
import com.bank_service.domain.enums.RoomType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class TestRoomProcessor implements GameResultProcessor {

    @Override
    public boolean supports(RoomType roomType) {
        return RoomType.ROOM_TEST == roomType;
    }

    @Override
    public RoomType getRoomType() {
        return RoomType.ROOM_TEST;
    }

    @Override
    public ProcessingResult process(GameTransactionRequest request) {
        if (!(request instanceof TestRoomTransactionRequest testRequest)) {
            return new ProcessingResult.Invalid("Invalid request type for TestRoom");
        }

        log.info("Test room processed successfully for room: {}", testRequest.roomId());
        return new ProcessingResult.Success(List.of());
    }
}
