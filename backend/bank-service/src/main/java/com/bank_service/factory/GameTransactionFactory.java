package com.bank_service.factory;

import com.bank_service.domain.dto.GameTransactionRequest;
import com.bank_service.domain.entity.Transaction;

import java.util.List;

public interface GameTransactionFactory {

    List<Transaction> createTransactions(GameTransactionRequest gameTransactionRequest);
}
