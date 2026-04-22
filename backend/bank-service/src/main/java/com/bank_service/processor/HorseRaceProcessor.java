package com.bank_service.processor;

import com.bank_service.domain.dto.game.GameTransactionRequest;
import com.bank_service.domain.dto.game.GameTransactionResponse;
import com.bank_service.domain.dto.game.HorseRaceTransactionRequest;
import com.bank_service.domain.entity.Transaction;
import com.bank_service.domain.enums.RoomType;
import com.bank_service.factory.HorseRaceTransactionFactory;
import com.bank_service.mapper.GameTransactionMapper;
import com.bank_service.service.RoomProcessingService;
import com.bank_service.service.TransactionLifecycleService;
import com.bank_service.service.grpc.client.GrpcUserTransactionClient;
import com.common_utils.exception.BadRequestException;
import com.common_utils.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.bank_service.config.ResourceMessageConstants.BAD_REQUEST_TYPE;
import static com.bank_service.config.ResourceMessageConstants.ROOM_ALREADY_PROCESSED;

@Component
@RequiredArgsConstructor
@Slf4j
public class HorseRaceProcessor implements GameTransactionProcessor {

    private final TransactionLifecycleService transactionLifecycleService;

    private final GameTransactionMapper gameTransactionMapper;

    private final HorseRaceTransactionFactory horseRaceTransactionFactory;

    private final GrpcUserTransactionClient grpcUserTransactionClient;

    private final RoomProcessingService roomProcessingService;

    @Override
    public boolean supports(RoomType roomType) {
        return RoomType.HORSE_RACE.equals(roomType);
    }

    @Override
    public RoomType getRoomType() {
        return RoomType.HORSE_RACE;
    }

    @Override
    public GameTransactionResponse process(GameTransactionRequest gameTransactionRequest) {
        if (!(gameTransactionRequest instanceof HorseRaceTransactionRequest request)) {
            throw new BadRequestException(String.format(BAD_REQUEST_TYPE, "Horse Race"));
        }

        boolean marked = roomProcessingService.markRoomAsProcessed(
                request.roomId(),
                request.roomType(),
                request.playerBets().size()
        );

        if (!marked) {
            log.info("Room {} already processed, skipping", request.roomId());
            throw new ConflictException(String.format(ROOM_ALREADY_PROCESSED, request.roomId()));
        }

        List<Transaction> transactions = horseRaceTransactionFactory.createTransactions(request);

        List<Transaction> saved = transactionLifecycleService.pending(transactions);

        try {
            grpcUserTransactionClient.sendUpdates(saved);
            transactionLifecycleService.success(saved);

            log.info("Successfully processed Horse Race game for room={}, winnerHorseIndex={}, transactions={}", request.roomId(), request.winnerHorseIndex(), saved.size());

            return gameTransactionMapper.toResponse(request, saved.size());
        } catch (Exception e) {
            transactionLifecycleService.rejectSafely(saved);

            log.error("User-service failed, transactions rejected for room={}", request.roomId(), e);

            throw e;
        }
    }
}
