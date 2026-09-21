package com.casualgames.bankservice.domain.dto;

import lombok.Builder;
import org.springframework.data.web.PagedModel;

@Builder
public record TransactionResponseList(

        PagedModel<TransactionResponse> transactions
) {
}
