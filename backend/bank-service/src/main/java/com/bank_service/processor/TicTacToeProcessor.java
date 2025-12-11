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
import com.bank_service.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class TicTacToeProcessor implements GameResultProcessor {

    private final TransactionService transactionService;

    private final TransactionMapper transactionMapper;

    private final TicTacToeTransactionFactory factory;

    private final UserServiceClient userServiceClient;

    @Override
    public boolean supports(RoomType roomType) {
        return RoomType.TIC_TAC_TOE.equals(roomType);
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

        try {
            List<Transaction> transactions = factory.createTransactions(ticTacToeTransactionRequest);

            List<Transaction> saved = savePendingTransactions(transactions, ticTacToeTransactionRequest.roomId());

            if (saved.isEmpty()) {
                log.info("Transactions already processed for room: {}", ticTacToeTransactionRequest.roomId());

                return new ProcessingResult.Draw("Already processed");
            }

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

    private List<Transaction> savePendingTransactions(List<Transaction> transactions, UUID roomId) {
        try {
            return transactionService.pending(transactions);
        } catch (DataIntegrityViolationException e) {
            log.warn("Duplicate transaction attempt for room: {}. Ignoring.", roomId);

            return List.of();
        }
    }
}
