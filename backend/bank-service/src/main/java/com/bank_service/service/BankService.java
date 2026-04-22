package com.bank_service.service;

import com.bank_service.domain.dto.game.GameTransactionRequest;
import com.bank_service.domain.dto.game.GameTransactionResponse;
import com.bank_service.domain.enums.RoomType;
import com.bank_service.processor.GameTransactionProcessor;
import com.common_utils.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.bank_service.config.ResourceMessageConstants.UNSUPPORTED_ROOM_TYPE;

@Service
@Transactional
@Slf4j
public class BankService {

    private final Map<RoomType, GameTransactionProcessor> processors;

    public BankService(List<GameTransactionProcessor> processors) {
        this.processors = processors.stream()
                .collect(Collectors.toMap(
                        GameTransactionProcessor::getRoomType,
                        Function.identity()
                ));

        log.info("Initialized BankService with {} processors: {}", processors.size(), this.processors.keySet());
    }

    public GameTransactionResponse processResults(GameTransactionRequest request) {
        log.info("Processing game results for room: {}, type: {}", request.roomId(), request.roomType());

        GameTransactionProcessor processor = findProcessor(request.roomType());

        return processor.process(request);
    }

    private GameTransactionProcessor findProcessor(RoomType roomType) {
        GameTransactionProcessor processor = processors.get(roomType);

        if (processor == null) {
            throw new BadRequestException(String.format(UNSUPPORTED_ROOM_TYPE, roomType));
        }

        return processor;
    }
}
