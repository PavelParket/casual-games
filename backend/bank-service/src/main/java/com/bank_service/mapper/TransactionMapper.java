package com.bank_service.mapper;

import com.bank_service.domain.dto.user_service.TransactionShortInfoRequest;
import com.bank_service.domain.entity.Transaction;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    TransactionShortInfoRequest toShortInfo(Transaction transaction);

    List<TransactionShortInfoRequest> toShortInfoList(List<Transaction> transaction);
}
