package com.bank_service.controller;

import com.bank_service.domain.dto.DepositRequest;
import com.bank_service.domain.dto.GenerateSummaryRequest;
import com.bank_service.domain.dto.PageResponse;
import com.bank_service.domain.dto.TransactionResponse;
import com.bank_service.domain.dto.TransactionSummaryFilterRequest;
import com.bank_service.domain.dto.TransactionSummaryResponse;
import com.bank_service.service.TransactionService;
import com.bank_service.service.TransactionSummaryService;
import com.security_starter.config.AuthenticationToken;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
                                                           @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                                                           @AuthenticationPrincipal AuthenticationToken authenticationToken) {
        return transactionService.getByUserGuid(guid, pageable, authenticationToken);
    }

    @PostMapping("/summary/search")
    public List<TransactionSummaryResponse> getSummaryByUserGuid(@RequestBody @Valid TransactionSummaryFilterRequest request,
                                                                 @AuthenticationPrincipal AuthenticationToken authenticationToken) {
        return summaryService.getByUserGuid(request, authenticationToken);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/summary/generate")
    public void generateSummaryManually(@RequestBody @Valid GenerateSummaryRequest request) {
        summaryService.generateSummary(request);
    }

    @PostMapping("/deposit")
    public TransactionResponse deposit(@RequestBody @Valid DepositRequest request,
                                       @AuthenticationPrincipal AuthenticationToken authenticationToken) {
        return transactionService.processDeposit(request, authenticationToken);
    }

    @GetMapping("/top-wins")
    public List<TransactionResponse> getTopWins(@RequestParam(defaultValue = "10") int limit) {
        return transactionService.getTopWins(limit);
    }
}
