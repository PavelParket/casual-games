package com.bank_service.domain.dto;

import lombok.Builder;
import org.springframework.data.web.PagedModel;

@Builder
public record TransactionResponseList(

        PagedModel<TransactionResponse> transactions
) {
}
