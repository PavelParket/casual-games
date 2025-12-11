package com.bank_service.controller;

import com.bank_service.domain.dto.GameTransactionRequest;
import com.bank_service.domain.dto.ProcessingResultResponse;
import com.bank_service.service.BankService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bank")
@RequiredArgsConstructor
public class BankController {

    private final BankService bankService;

    @PostMapping("/save")
    public ProcessingResultResponse saveResults(@RequestBody @Valid GameTransactionRequest gameTransactionRequest) {
        return bankService.processResults(gameTransactionRequest);
    }
}
