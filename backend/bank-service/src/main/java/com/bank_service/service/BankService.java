package com.bank_service.service;

import com.bank_service.domain.dto.GameTransactionRequest;
import com.bank_service.domain.dto.GameTransactionResponse;
import com.bank_service.domain.enums.RoomType;
import com.bank_service.mapper.ProcessingResultMapper;
import com.bank_service.processor.GameResultProcessor;
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
@Slf4j
@Transactional
public class BankService {

    private final Map<RoomType, GameResultProcessor> processors;

    public BankService(List<GameResultProcessor> processors, ProcessingResultMapper processingResultMapper) {
        this.processors = processors.stream()
                .collect(Collectors.toMap(
                        GameResultProcessor::getRoomType,
                        Function.identity()
                ));

        log.info("Initialized BankService with {} processors: {}", processors.size(), this.processors.keySet().getClass().getCanonicalName());
    }

    public GameTransactionResponse processResults(GameTransactionRequest request) {
        log.info("Processing game results for room: {}, type: {}", request.roomId(), request.roomType());

        GameResultProcessor processor = findProcessor(request.roomType());

        return processor.process(request);
    }

    private GameResultProcessor findProcessor(RoomType roomType) {
        GameResultProcessor processor = processors.get(roomType);

        if (processor == null) {
            throw new BadRequestException(String.format(UNSUPPORTED_ROOM_TYPE, roomType));
        }

        return processor;
    }
}
