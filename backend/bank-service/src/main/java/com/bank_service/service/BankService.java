package com.bank_service.service;

import com.bank_service.domain.dto.GameTransactionRequest;
import com.bank_service.domain.dto.ProcessingResult;
import com.bank_service.domain.dto.ProcessingResultResponse;
import com.bank_service.domain.enums.RoomType;
import com.bank_service.exception.UnsupportedRoomTypeException;
import com.bank_service.mapper.ProcessingResultMapper;
import com.bank_service.processor.GameResultProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class BankService {

    private final Map<RoomType, GameResultProcessor> processors;

    private final ProcessingResultMapper processingResultMapper;

    public BankService(List<GameResultProcessor> processors, ProcessingResultMapper processingResultMapper) {
        this.processors = processors.stream()
                .collect(Collectors.toMap(
                        GameResultProcessor::getRoomType,
                        Function.identity()
                ));
        this.processingResultMapper = processingResultMapper;

        log.info("Initialized BankService with {} processors: {}", processors.size(), this.processors.keySet());
    }

    public ProcessingResultResponse processResults(GameTransactionRequest request) {
        log.info("Processing game results for room: {}, type: {}", request.roomId(), request.roomType());

        GameResultProcessor processor = findProcessor(request.roomType());

        ProcessingResult result = processor.process(request);

        logResult(result, request.roomType());

        return processingResultMapper.toResponse(result);
    }

    private GameResultProcessor findProcessor(RoomType roomType) {
        GameResultProcessor processor = processors.get(roomType);

        if (processor == null) {
            throw new UnsupportedRoomTypeException("No processor found for room type: " + roomType);
        }

        return processor;
    }

    private void logResult(ProcessingResult result, RoomType roomType) {
        switch (result) {
            case ProcessingResult.Success success -> log.info("Successfully processed {} game, created {} transactions",
                    roomType, success.transactions().size());
            case ProcessingResult.Draw draw -> log.info("Game {} ended in draw: {}", roomType, draw.reason());
            case ProcessingResult.Invalid invalid -> log.warn("Invalid game {} result: {}", roomType, invalid.reason());
            case ProcessingResult.AlreadyProcessed alreadyProcessed ->
                    log.debug("Room {} already processed: {}", roomType, alreadyProcessed.message());
        }
    }
}
