package com.bank_service.controller;

import com.bank_service.domain.dto.DepositRequest;
import com.bank_service.domain.dto.GenerateSummaryRequest;
import com.bank_service.domain.dto.TransactionResponse;
import com.bank_service.domain.dto.TransactionResponseList;
import com.bank_service.domain.dto.TransactionSummaryFilterRequest;
import com.bank_service.domain.dto.TransactionSummaryResponse;
import com.bank_service.service.TransactionService;
import com.bank_service.service.TransactionSummaryService;
import com.common_utils.dto.ErrorResponse;
import com.security_starter.config.AuthenticationToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
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
@ApiResponses(value = {
        @ApiResponse(responseCode = "400",
                description = "Bad Request",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401",
                description = "Unauthorized",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403",
                description = "Forbidden",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404",
                description = "Not Found",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500",
                description = "Internal Server Error",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class)))
})
public class TransactionController {

    private final TransactionService transactionService;

    private final TransactionSummaryService summaryService;

    @GetMapping("/{userGuid}")
    @Operation(summary = "Get user transactions", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200")
    public TransactionResponseList getByUserGuid(@PathVariable UUID userGuid,
                                                 @ParameterObject @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                                                 @AuthenticationPrincipal AuthenticationToken authenticationToken) {
        return transactionService.getByUserGuid(userGuid, pageable, authenticationToken);
    }

    @PostMapping("/summary/search")
    @Operation(summary = "Search user transaction summaries by filter", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200")
    public List<TransactionSummaryResponse> getSummaryByUserGuid(@RequestBody @Valid TransactionSummaryFilterRequest request,
                                                                 @AuthenticationPrincipal AuthenticationToken authenticationToken) {
        return summaryService.getByUserGuid(request, authenticationToken);
    }

    @Deprecated
    @PostMapping("/summary/generate")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Generate transaction summary", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200")
    public void generateSummaryManually(@RequestBody @Valid GenerateSummaryRequest request) {
        summaryService.generateSummary(request);
    }

    @PostMapping("/deposit")
    @Operation(summary = "Process a deposit transaction", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200")
    public TransactionResponse deposit(@RequestBody @Valid DepositRequest request,
                                       @AuthenticationPrincipal AuthenticationToken authenticationToken) {
        return transactionService.processDeposit(request, authenticationToken);
    }

    @GetMapping("/top-wins")
    @Operation(summary = "Get today top winning transactions", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200")
    public List<TransactionResponse> getTopWins(@RequestParam(defaultValue = "10") int limit) {
        return transactionService.getTopWins(limit);
    }
}
