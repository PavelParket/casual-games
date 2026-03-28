package com.bank_service.processor;

import com.bank_service.client.UserServiceClient;
import com.bank_service.domain.dto.DurakTransactionRequest;
import com.bank_service.domain.dto.GameTransactionRequest;
import com.bank_service.domain.dto.ProcessingResult;
import com.bank_service.domain.entity.Transaction;
import com.bank_service.domain.enums.RoomType;
import com.bank_service.exception.ClientInternalRequestException;
import com.bank_service.exception.PlayerNotFoundException;
import com.bank_service.factory.DurakTransactionFactory;
import com.bank_service.mapper.TransactionMapper;
import com.bank_service.service.RoomProcessingService;
import com.bank_service.service.TransactionService;
import com.bank_service.validator.DurakBusinessValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DurakProcessor implements GameResultProcessor {

    private final TransactionService transactionService;

    private final TransactionMapper transactionMapper;

    private final DurakTransactionFactory transactionFactory;

    private final UserServiceClient userServiceClient;

    private final RoomProcessingService roomProcessingService;

    private final DurakBusinessValidator businessValidator;

    @Override
    public boolean supports(RoomType roomType) {
        return RoomType.DURAK.equals(roomType);
    }

    @Override
    public RoomType getRoomType() {
        return RoomType.DURAK;
    }

    @Override
    public ProcessingResult process(GameTransactionRequest gameTransactionRequest) {
        if (!(gameTransactionRequest instanceof DurakTransactionRequest request)) {
            return new ProcessingResult.Invalid("Invalid request type for Durak");
        }

        if (request.isDraw()) {
            log.info("Draw detected for room: {}", request.roomId());

            return new ProcessingResult.Draw("Game ended in a draw");
        }

        businessValidator.validate(request);

        boolean marked = roomProcessingService.markRoomAsProcessed(request.roomId(), request.roomType(), 2);

        if (!marked) {
            log.info("Room {} already processed, skipping", request.roomId());

            return new ProcessingResult.AlreadyProcessed("Already processed");
        }

        try {
            List<Transaction> transactions = transactionFactory.createTransactions(request);
            List<Transaction> saved = transactionService.pending(transactions);

            try {
                userServiceClient.sendUpdates(transactionMapper.toShortInfoList(saved));
                transactionService.success(saved);

                log.info("Successfully processed Durak game for room: {}", request.roomId());

                return new ProcessingResult.Success(saved);
            } catch (ClientInternalRequestException e) {
                transactionService.rejectSafely(saved);

                log.error("User-service failed, transactions rejected for room: {}", request.roomId(), e);

                throw e;
            }
        } catch (PlayerNotFoundException e) {
            log.error("Player not found in Durak game: {}", e.getMessage());

            return new ProcessingResult.Invalid(e.getMessage());
        }
    }
}
