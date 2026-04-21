package com.bank_service.processor;

import com.bank_service.domain.dto.DeCoderTransactionRequest;
import com.bank_service.domain.dto.GameTransactionRequest;
import com.bank_service.domain.dto.GameTransactionResponse;
import com.bank_service.domain.entity.Transaction;
import com.bank_service.domain.enums.RoomType;
import com.bank_service.factory.DeCoderTransactionFactory;
import com.bank_service.mapper.GameTransactionMapper;
import com.bank_service.service.TransactionService;
import com.bank_service.service.grpc.client.GrpcUserTransactionClient;
import com.bank_service.validator.DeCoderBusinessValidator;
import com.common_utils.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.bank_service.config.ResourceMessageConstants.BAD_REQUEST_TYPE;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeCoderProcessor implements GameResultProcessor {

    private final TransactionService transactionService;

    private final GameTransactionMapper gameTransactionMapper;

    private final DeCoderTransactionFactory factory;

    private final GrpcUserTransactionClient grpcUserTransactionClient;

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
    public GameTransactionResponse process(GameTransactionRequest gameTransactionRequest) {
        if (!(gameTransactionRequest instanceof DeCoderTransactionRequest request)) {
            throw new BadRequestException(String.format(BAD_REQUEST_TYPE, "De-Coder"));
        }

        businessValidator.validate(request);

        List<Transaction> transactions = factory.createTransactions(request);

        List<Transaction> saved = transactionService.pending(transactions);

        try {
            grpcUserTransactionClient.sendUpdates(saved);

            transactionService.success(saved);

            log.info("Successfully processed De-Coder transaction for room: {}", request.roomId());

            return gameTransactionMapper.toResponse(request, saved.size());
        } catch (Exception e) {
            transactionService.rejectSafely(saved);

            log.error("User-service failed, transactions rejected for room: {}", request.roomId(), e);

            throw e;
        }
    }
}