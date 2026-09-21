package com.casualgames.bankservice.mapper;

import com.casualgames.bankservice.domain.dto.TransactionSummaryResponse;
import com.casualgames.bankservice.domain.entity.TransactionSummary;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransactionSummaryMapper {

    TransactionSummaryResponse toResponse(TransactionSummary transactionSummary);

    List<TransactionSummaryResponse> toResponseList(List<TransactionSummary> transactionSummaries);
}
