package com.bank_service.mapper;

import com.bank_service.domain.entity.Transaction;
import com.bank_service.domain.entity.TransactionShortInfo;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    TransactionShortInfo toShortInfo(Transaction transaction);

    List<TransactionShortInfo> toShortInfoList(List<Transaction> transaction);
}
