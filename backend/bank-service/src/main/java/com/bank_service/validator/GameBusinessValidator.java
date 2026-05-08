package com.bank_service.validator;

import com.bank_service.domain.enums.RoomType;

import java.math.BigDecimal;

public interface GameBusinessValidator<T> {

    BigDecimal MAX_BALANCE = new BigDecimal("999999999.99");

    RoomType getRoomType();

    void validate(T request);
}
