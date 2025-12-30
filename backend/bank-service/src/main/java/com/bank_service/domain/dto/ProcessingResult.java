package com.bank_service.domain.dto;

import com.bank_service.domain.entity.Transaction;

import java.util.List;

public sealed interface ProcessingResult {

    record Success(List<Transaction> transactions) implements ProcessingResult {
    }

    record Draw(String reason) implements ProcessingResult {
    }

    record Invalid(String reason) implements ProcessingResult {
    }

    record AlreadyProcessed(String message) implements ProcessingResult {

    }
}
