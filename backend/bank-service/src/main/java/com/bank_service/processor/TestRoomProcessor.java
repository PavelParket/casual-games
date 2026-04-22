package com.bank_service.processor;

import com.bank_service.domain.dto.game.GameTransactionRequest;
import com.bank_service.domain.dto.game.GameTransactionResponse;
import com.bank_service.domain.dto.game.TestRoomTransactionRequest;
import com.bank_service.domain.enums.RoomType;
import com.common_utils.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;

import static com.bank_service.config.ResourceMessageConstants.BAD_REQUEST_TYPE;

@Component
@Slf4j
public class TestRoomProcessor implements GameTransactionProcessor {

    @Override
    public boolean supports(RoomType roomType) {
        return RoomType.ROOM_TEST == roomType;
    }

    @Override
    public RoomType getRoomType() {
        return RoomType.ROOM_TEST;
    }

    @Override
    public GameTransactionResponse process(GameTransactionRequest gameTransactionRequest) {
        if (!(gameTransactionRequest instanceof TestRoomTransactionRequest request)) {
            throw new BadRequestException(String.format(BAD_REQUEST_TYPE, "TestRoom"));
        }

        log.info("Test room processed successfully for room: {}", request.roomId());

        return GameTransactionResponse.builder()
                .roomId(request.roomId())
                .roomType(request.roomType())
                .transactionCount(0)
                .processedAt(Instant.now())
                .build();
    }
}
