package com.bank_service.validator;

import com.bank_service.domain.dto.GameTransactionRequest;

public interface GameBusinessValidator<T extends GameTransactionRequest> {

    void validate(T request);
}
