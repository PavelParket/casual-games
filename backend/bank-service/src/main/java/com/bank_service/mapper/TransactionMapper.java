package com.bank_service.mapper;

import com.bank_service.domain.dto.TransactionResponse;
import com.bank_service.domain.dto.user_service.TransactionShortInfoInternalRequest;
import com.bank_service.domain.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    TransactionShortInfoInternalRequest toShortInfo(Transaction transaction);

    List<TransactionShortInfoInternalRequest> toShortInfoList(List<Transaction> transaction);

    @Mapping(
            target = "createdAtDate",
            expression = "java(transaction.getCreatedAt().atOffset(java.time.ZoneOffset.UTC).toLocalDate())"
    )
    @Mapping(
            target = "createdAtTime",
            expression = "java(transaction.getCreatedAt().atOffset(java.time.ZoneOffset.UTC).toLocalTime())"
    )
    TransactionResponse toResponse(Transaction transaction);

    List<TransactionResponse> toResponseList(List<Transaction> transactions);
}
