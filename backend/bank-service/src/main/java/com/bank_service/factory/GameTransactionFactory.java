package com.bank_service.factory;

import com.bank_service.domain.entity.Transaction;
import com.bank_service.domain.enums.RoomType;

import java.util.List;

public interface GameTransactionFactory<T> {

    RoomType getRoomType();

    List<Transaction> createTransactions(T request);
}
