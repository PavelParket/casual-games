package com.bank_service.processor;

import com.bank_service.client.UserServiceClient;
import com.bank_service.domain.dto.GameTransactionRequest;
import com.bank_service.domain.dto.ProcessingResult;
import com.bank_service.domain.dto.TicTacToeTransactionRequest;
import com.bank_service.domain.entity.Transaction;
import com.bank_service.domain.enums.RoomType;
import com.bank_service.exception.ClientInternalRequestException;
import com.bank_service.exception.PlayerNotFoundException;
import com.bank_service.factory.TicTacToeTransactionFactory;
import com.bank_service.mapper.TransactionMapper;
import com.bank_service.service.RoomProcessingService;
import com.bank_service.service.TransactionService;
import com.bank_service.validator.TicTacToeBusinessValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TicTacToeProcessor implements GameResultProcessor {

    private final TransactionService transactionService;

    private final TransactionMapper transactionMapper;

    private final TicTacToeTransactionFactory factory;

    private final UserServiceClient userServiceClient;

    private final RoomProcessingService roomProcessingService;

    private final TicTacToeBusinessValidator businessValidator;

    @Override
    public boolean supports(RoomType roomType) {
        return RoomType.TIC_TAC_TOE.equals(roomType);
    }

    @Override
    public RoomType getRoomType() {
        return RoomType.TIC_TAC_TOE;
    }

    @Override
    public ProcessingResult process(GameTransactionRequest request) {
        if (!(request instanceof TicTacToeTransactionRequest ticTacToeTransactionRequest)) {
            return new ProcessingResult.Invalid("Invalid request type for Tic-Tac-Toe");
        }

        if (ticTacToeTransactionRequest.isDraw()) {
            log.info("Draw detected for room: {}", ticTacToeTransactionRequest.roomId());

            return new ProcessingResult.Draw("Game ended in a draw");
        }

        businessValidator.validate(ticTacToeTransactionRequest);

        boolean marked = roomProcessingService.markRoomAsProcessed(
                ticTacToeTransactionRequest.roomId(),
                ticTacToeTransactionRequest.roomType(),
                2
        );

        if (!marked) {
            log.info("Room {} already processed, skipping", ticTacToeTransactionRequest.roomId());

            return new ProcessingResult.AlreadyProcessed("Already processed");
        }

        try {
            List<Transaction> transactions = factory.createTransactions(ticTacToeTransactionRequest);

            List<Transaction> saved = transactionService.pending(transactions);

            try {
                userServiceClient.sendUpdates(transactionMapper.toShortInfoList(saved));
                transactionService.success(saved);

                log.info("Successfully processed Tic-Tac-Toe game for room: {}", ticTacToeTransactionRequest.roomId());

                return new ProcessingResult.Success(saved);
            } catch (ClientInternalRequestException e) {
                transactionService.rejectSafely(saved);

                log.error("User-service failed, transactions rejected for room: {}", ticTacToeTransactionRequest.roomId(), e);

                throw e;
            }
        } catch (PlayerNotFoundException e) {
            log.error("Player not found in Tic-Tac-Toe game: {}", e.getMessage());

            return new ProcessingResult.Invalid(e.getMessage());
        }
    }
}
