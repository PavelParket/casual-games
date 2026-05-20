package com.bank_service.repository.projection;

import java.math.BigDecimal;

public interface TransactionSummaryProjection {

    BigDecimal getTotalWon();

    BigDecimal getTotalLost();

    BigDecimal getBalanceBefore();

    BigDecimal getBalanceAfter();
}
