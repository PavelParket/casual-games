package com.bank_service.validator;

import com.bank_service.domain.enums.RoomType;
import com.casualgames.grpc.transaction.DeCoderTransactionRequest;
import com.common_utils.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import static com.bank_service.config.ResourceMessageConstants.INSUFFICIENT_FUNDS;
import static com.bank_service.config.ResourceMessageConstants.INVALID_BET_AMOUNT;

@Component
@Slf4j
public class DeCoderBusinessValidator implements GameBusinessValidator<DeCoderTransactionRequest> {

    @Override
    public RoomType getRoomType() {
        return RoomType.DE_CODER;
    }

    @Override
    public void validate(DeCoderTransactionRequest request) {
        request.getPlayerTransactionsList().forEach(transaction -> {
            BigDecimal spent = new BigDecimal(transaction.getSpent());
            BigDecimal balanceBefore = new BigDecimal(transaction.getBalanceBefore());

            if (spent.compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("Invalid spent amount for De-Coder player={}: spent={}", transaction.getGuid(), spent);
                throw new BadRequestException(INVALID_BET_AMOUNT);
            }

            if (balanceBefore.compareTo(BigDecimal.ZERO) < 0) {
                log.warn("Negative balance snapshot for De-Coder player={}: balance={}", transaction.getGuid(), balanceBefore);
                throw new BadRequestException(String.format(INSUFFICIENT_FUNDS, balanceBefore, spent));
            }

            if (transaction.hasJackpot()) {
                BigDecimal jackpot = new BigDecimal(transaction.getJackpot());

                if (jackpot.compareTo(BigDecimal.ZERO) <= 0) {
                    log.warn("Invalid jackpot amount for De-Coder winner={}: jackpot={}", transaction.getGuid(), jackpot);
                    throw new BadRequestException(INVALID_BET_AMOUNT);
                }
            }
        });
    }
}