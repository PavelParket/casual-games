package com.bank_service.mapper;

import com.bank_service.domain.dto.TransactionSummaryResponse;
import com.bank_service.domain.entity.TransactionSummary;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransactionSummaryMapper {

    TransactionSummaryResponse toResponse(TransactionSummary transactionSummary);

    List<TransactionSummaryResponse> toResponseList(List<TransactionSummary> transactionSummaries);
}
