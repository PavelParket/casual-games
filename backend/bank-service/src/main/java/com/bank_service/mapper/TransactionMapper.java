package com.bank_service.mapper;

import com.bank_service.domain.entity.Transaction;
import com.bank_service.domain.entity.TransactionShortInfo;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    TransactionShortInfo toShortInfo(Transaction transaction);

    List<TransactionShortInfo> toShortInfoList(List<Transaction> transaction);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
            nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userGuid", ignore = true)
    @Mapping(target = "roomId", ignore = true)
    @Mapping(target = "roomType", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "amount", ignore = true)
    @Mapping(target = "balanceBefore", ignore = true)
    @Mapping(target = "balanceAfter", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateTransactionStatusFromShortInfo(TransactionShortInfo transactionShortInfo, @MappingTarget Transaction transaction);
}
