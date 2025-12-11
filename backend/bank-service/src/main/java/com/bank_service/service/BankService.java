package com.bank_service.service;

import com.bank_service.client.UserServiceClient;
import com.bank_service.domain.dto.GameTransactionRequest;
import com.bank_service.domain.entity.Transaction;
import com.bank_service.domain.enums.RoomType;
import com.bank_service.exception.BetsNotFoundException;
import com.bank_service.exception.ClientInternalRequestException;
import com.bank_service.exception.UnsupportedFactoryTypeException;
import com.bank_service.exception.UnsupportedRoomTypeException;
import com.bank_service.factory.GameTransactionFactory;
import com.bank_service.mapper.TransactionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BankService {

    private final Map<RoomType, GameTransactionFactory> transactionFactories;

    private final TransactionMapper transactionMapper;

    private final UserServiceClient userServiceClient;

    private final TransactionService transactionService;

    public void processResults(GameTransactionRequest request) {
        GameTransactionFactory factory = transactionFactories.get(request.roomType());

        if (Objects.isNull(factory)) {
            throw new UnsupportedRoomTypeException("Unsupported room type: " + request.roomType());
        }

        switch (request.roomType()) {
            case RoomType.TIC_TAC_TOE -> processTicTacToeGameResults(request, factory);
            case RoomType.ROOM_TEST -> System.out.println("Everything ok!");
            default -> throw new UnsupportedFactoryTypeException("Unsupported factory type: " + factory.getClass());
        }
    }

    private void processTicTacToeGameResults(GameTransactionRequest request, GameTransactionFactory factory) {
        // TODO: чё-нить вернуть, чтобы было понятно, что ничья
        if (Objects.isNull(request.winners()) || request.winners().isEmpty()) {
            return;
        }

        if (Objects.isNull(request.playerBets()) || request.playerBets().isEmpty()) {
            throw new BetsNotFoundException("Player bets are empty!");
        }

        List<Transaction> transactions = factory.createTransactions(request);
        List<Transaction> saved = transactionService.pending(transactions);

        try {
            userServiceClient.sendUpdates(transactionMapper.toShortInfoList(saved));
            transactionService.success(saved);
        } catch (ClientInternalRequestException e) {
            transactionService.reject(saved);
            log.info("User-service failed, rejecting transactions: {}", e.getMessage(), e);

            throw new ClientInternalRequestException(e.getMessage());
        }
    }
}
