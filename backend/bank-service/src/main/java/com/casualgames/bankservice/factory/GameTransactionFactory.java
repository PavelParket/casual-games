package com.casualgames.bankservice.factory;

import com.casualgames.bankservice.domain.entity.Transaction;
import com.casualgames.bankservice.domain.enums.RoomType;

import java.util.List;

public interface GameTransactionFactory<T> {

    RoomType getRoomType();

    List<Transaction> createTransactions(T request);
}
