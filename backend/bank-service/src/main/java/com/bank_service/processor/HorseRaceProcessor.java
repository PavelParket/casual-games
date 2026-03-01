package com.bank_service.processor;

import com.bank_service.client.UserServiceClient;
import com.bank_service.domain.dto.GameTransactionRequest;
import com.bank_service.domain.dto.HorseRaceTransactionRequest;
import com.bank_service.domain.dto.ProcessingResult;
import com.bank_service.domain.entity.Transaction;
import com.bank_service.domain.enums.RoomType;
import com.bank_service.exception.ClientInternalRequestException;
import com.bank_service.factory.HorseRaceTransactionFactory;
import com.bank_service.mapper.TransactionMapper;
import com.bank_service.service.RoomProcessingService;
import com.bank_service.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class HorseRaceProcessor implements GameResultProcessor {

    private final TransactionService transactionService;

    private final TransactionMapper transactionMapper;

    private final HorseRaceTransactionFactory horseRaceTransactionFactory;

    private final UserServiceClient userServiceClient;

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
    public ProcessingResult process(GameTransactionRequest request) {
        if (!(request instanceof HorseRaceTransactionRequest horseRaceRequest)) {
            return new ProcessingResult.Invalid("Invalid request type for Horse Race");
        }

        boolean marked = roomProcessingService.markRoomAsProcessed(
                horseRaceRequest.roomId(),
                horseRaceRequest.roomType(),
                horseRaceRequest.playerBets().size()
        );

        if (!marked) {
            log.info("Room {} already processed, skipping", horseRaceRequest.roomId());
            return new ProcessingResult.AlreadyProcessed("Already processed");
        }

        try {
            List<Transaction> transactions = horseRaceTransactionFactory.createTransactions(horseRaceRequest);

            List<Transaction> saved = transactionService.pending(transactions);

            try {
                userServiceClient.sendUpdates(transactionMapper.toShortInfoList(saved));
                transactionService.success(saved);

                log.info("Successfully processed Horse Race game for room={}, winnerHorseIndex={}, transactions={}", horseRaceRequest.roomId(), horseRaceRequest.winnerHorseIndex(), saved.size());

                return new ProcessingResult.Success(saved);
            } catch (ClientInternalRequestException e) {
                transactionService.rejectSafely(saved);

                log.error("User-service failed, transactions rejected for room={}", horseRaceRequest.roomId(), e);

                throw e;
            }
        } catch (Exception e) {
            log.error("Failed to process Horse Race game for room={}: {}", horseRaceRequest.roomId(), e.getMessage());
            return new ProcessingResult.Invalid(e.getMessage());
        }
    }
}
