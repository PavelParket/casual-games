package com.bank_service.service;

import com.bank_service.domain.entity.Transaction;
import com.bank_service.domain.enums.RoomType;
import com.bank_service.factory.GameTransactionFactory;
import com.bank_service.mapper.GameTransactionMapper;
import com.bank_service.service.grpc.client.GrpcUserTransactionClient;
import com.bank_service.validator.GameBusinessValidator;
import com.casualgames.grpc.transaction.DeCoderTransactionRequest;
import com.casualgames.grpc.transaction.DurakTransactionRequest;
import com.casualgames.grpc.transaction.GameTransactionResponse;
import com.casualgames.grpc.transaction.HorseRaceTransactionRequest;
import com.casualgames.grpc.transaction.TicTacToeTransactionRequest;
import com.common_utils.exception.ConflictException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.bank_service.config.ResourceMessageConstants.ROOM_ALREADY_PROCESSED;

@Service
@Transactional
@Slf4j
public class GameTransactionService {

    private final Map<RoomType, GameTransactionFactory<?>> gameTransactionFactoryMap;

    private final Map<RoomType, GameBusinessValidator<?>> gameBusinessValidatorMap;

    private final GameTransactionMapper gameTransactionMapper;

    private final TransactionLifecycleService transactionLifecycleService;

    private final GrpcUserTransactionClient grpcUserTransactionClient;

    private final RoomProcessingService roomProcessingService;

    public GameTransactionService(
            List<GameTransactionFactory<?>> gameTransactionFactories,
            List<GameBusinessValidator<?>> gameBusinessValidators,
            TransactionLifecycleService transactionLifecycleService,
            GrpcUserTransactionClient grpcUserTransactionClient,
            RoomProcessingService roomProcessingService,
            GameTransactionMapper gameTransactionMapper
    ) {
        this.transactionLifecycleService = transactionLifecycleService;
        this.grpcUserTransactionClient = grpcUserTransactionClient;
        this.roomProcessingService = roomProcessingService;
        this.gameTransactionMapper = gameTransactionMapper;
        this.gameTransactionFactoryMap = gameTransactionFactories.stream()
                .collect(Collectors.toMap(
                        GameTransactionFactory::getRoomType,
                        Function.identity()
                ));
        this.gameBusinessValidatorMap = gameBusinessValidators.stream()
                .collect(Collectors.toMap(
                        GameBusinessValidator::getRoomType,
                        Function.identity()
                ));
    }

    public GameTransactionResponse processTicTacToe(TicTacToeTransactionRequest request) {
        if (!request.hasWinner()) {
            return gameTransactionMapper.toGrpcResponse(request.getRoomId(), request.getRoomType(), 0);
        }

        return processWithDeduplication(request, RoomType.TIC_TAC_TOE, request.getRoomId(), 2);
    }

    public GameTransactionResponse processHorseRace(HorseRaceTransactionRequest request) {
        return processWithDeduplication(request, RoomType.HORSE_RACE, request.getRoomId(), request.getPlayerBetsCount());
    }

    public GameTransactionResponse processDurak(DurakTransactionRequest request) {
        if (!request.hasWinner()) {
            return gameTransactionMapper.toGrpcResponse(request.getRoomId(), request.getRoomType(), 0);
        }

        return processWithDeduplication(request, RoomType.DURAK, request.getRoomId(), 2);
    }

    public GameTransactionResponse processDeCoder(DeCoderTransactionRequest request) {
        return process(request, RoomType.DE_CODER, request.getRoomId());
    }

    private <T> GameTransactionResponse processWithDeduplication(T request,
                                                                 RoomType roomType,
                                                                 String roomId,
                                                                 int expectedCount) {
        boolean marked = roomProcessingService.markRoomAsProcessed(UUID.fromString(roomId), roomType, expectedCount);

        if (!marked) {
            log.info("Room {} already processed, skipping", roomId);

            throw new ConflictException(String.format(ROOM_ALREADY_PROCESSED, roomId));
        }

        return process(request, roomType, roomId);
    }

    @SuppressWarnings("unchecked")
    private <T> GameTransactionResponse process(T request,
                                                RoomType roomType,
                                                String roomId) {
        GameBusinessValidator<T> gameBusinessValidator = (GameBusinessValidator<T>) gameBusinessValidatorMap.getOrDefault(roomType, null);

        if (gameBusinessValidator != null) {
            gameBusinessValidator.validate(request);
        }

        GameTransactionFactory<T> factory = (GameTransactionFactory<T>) gameTransactionFactoryMap.get(roomType);

        List<Transaction> transactions = factory.createTransactions(request);
        List<Transaction> saved = transactionLifecycleService.pending(transactions);

        try {
            grpcUserTransactionClient.sendUpdates(saved);
            transactionLifecycleService.success(saved);

            log.info("Successfully processed {} for room: {}", roomType, roomId);

            return gameTransactionMapper.toGrpcResponse(roomId, roomType.name(), saved.size());
        } catch (Exception e) {
            transactionLifecycleService.rejectSafely(saved);

            log.error("User-service failed, transactions rejected for room: {}", roomId, e);

            throw e;
        }
    }
}
