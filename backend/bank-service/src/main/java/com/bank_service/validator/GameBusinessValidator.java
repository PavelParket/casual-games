package com.bank_service.validator;

import com.bank_service.domain.dto.GameTransactionRequest;

import java.math.BigDecimal;

public interface GameBusinessValidator<T extends GameTransactionRequest> {

    BigDecimal MAX_BALANCE = new BigDecimal("999999999.99");

    void validate(T request);
}
