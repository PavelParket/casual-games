package com.bank_service.validator;

import java.math.BigDecimal;

public interface GameBusinessValidator<T> {

    BigDecimal MAX_BALANCE = new BigDecimal("999999999.99");

    void validate(T request);
}
