package com.bank_service.processor;

import com.bank_service.client.UserServiceClient;
import com.bank_service.domain.dto.DeCoderTransactionRequest;
import com.bank_service.domain.dto.GameTransactionRequest;
import com.bank_service.domain.dto.ProcessingResult;
import com.bank_service.domain.entity.Transaction;
import com.bank_service.domain.enums.RoomType;
import com.bank_service.exception.ClientInternalRequestException;
import com.bank_service.factory.DeCoderTransactionFactory;
import com.bank_service.mapper.TransactionMapper;
import com.bank_service.service.TransactionService;
import com.bank_service.validator.DeCoderBusinessValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeCoderProcessor implements GameResultProcessor {

    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;
    private final DeCoderTransactionFactory factory;
    private final UserServiceClient userServiceClient;
    private final DeCoderBusinessValidator businessValidator;

    @Override
    public boolean supports(RoomType roomType) {
        return RoomType.DE_CODER.equals(roomType);
    }

    @Override
    public RoomType getRoomType() {
        return RoomType.DE_CODER;
    }

    @Override
    public ProcessingResult process(GameTransactionRequest request) {
        if (!(request instanceof DeCoderTransactionRequest deCoderRequest)) {
            return new ProcessingResult.Invalid("Invalid request type for De-Coder");
        }

        businessValidator.validate(deCoderRequest);

        try {
            List<Transaction> transactions = factory.createTransactions(deCoderRequest);

            List<Transaction> saved = transactionService.pending(transactions);

            try {
                userServiceClient.sendUpdates(transactionMapper.toShortInfoList(saved));

                transactionService.success(saved);

                log.info("Successfully processed De-Coder transaction for room: {}", deCoderRequest.roomId());

                return new ProcessingResult.Success(saved);
            } catch (ClientInternalRequestException e) {
                transactionService.rejectSafely(saved);

                log.error("User-service failed, transactions rejected for room: {}", deCoderRequest.roomId(), e);

                throw e;
            }
        } catch (Exception e) {
            log.error("Error processing De-Coder transaction: {}", e.getMessage());

            return new ProcessingResult.Invalid(e.getMessage());
        }
    }
}