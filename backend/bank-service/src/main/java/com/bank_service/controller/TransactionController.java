package com.bank_service.controller;

import com.bank_service.domain.dto.*;
import com.bank_service.service.TransactionService;
import com.bank_service.service.TransactionSummaryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;

    private final TransactionSummaryService summaryService;

    @GetMapping("/{guid}")
    public PageResponse<TransactionResponse> getByUserGuid(@PathVariable UUID guid,
                                                           @RequestParam(defaultValue = "0") @Min(0) int page,
                                                           @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
        return transactionService.getByUserGuid(guid, page, size);
    }

    @PostMapping("/summary/search")
    public List<TransactionSummaryResponse> getByUserGuid(@RequestBody @Valid TransactionSummaryFilterRequest request) {
        return summaryService.getByUserGuid(request);
    }

    @PostMapping("/deposit")
    public TransactionResponse deposit(@RequestBody @Valid DepositRequest request) {
        log.info("Received deposit request for user: {} with amount: {}", request.userGuid(), request.amount());
        return transactionService.processDeposit(request);
    }
}
