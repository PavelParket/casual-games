package com.bank_service.mapper;

import com.bank_service.domain.dto.user_service.TransactionShortInfoInternalRequest;
import com.bank_service.domain.entity.Transaction;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    TransactionShortInfoInternalRequest toShortInfo(Transaction transaction);

    List<TransactionShortInfoInternalRequest> toShortInfoList(List<Transaction> transaction);
}
