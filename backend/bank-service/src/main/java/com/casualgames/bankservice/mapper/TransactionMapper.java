package com.casualgames.bankservice.mapper;

import com.casualgames.bankservice.domain.dto.TransactionResponse;
import com.casualgames.bankservice.domain.entity.Transaction;
import com.casualgames.commonutils.mapper.PagedModelMapper;
import com.casualgames.grpc.transaction.UserTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransactionMapper extends PagedModelMapper {

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

    @Mapping(target = "userGuidBytes", ignore = true)
    @Mapping(target = "unknownFields", ignore = true)
    @Mapping(target = "typeBytes", ignore = true)
    @Mapping(target = "statusBytes", ignore = true)
    @Mapping(target = "mergeUnknownFields", ignore = true)
    @Mapping(target = "mergeFrom", ignore = true)
    @Mapping(target = "clearOneof", ignore = true)
    @Mapping(target = "clearField", ignore = true)
    @Mapping(target = "amountBytes", ignore = true)
    @Mapping(target = "allFields", ignore = true)
    @Mapping(target = "amount", expression = "java(transaction.getAmount().toPlainString())")
    UserTransaction toUserTransaction(Transaction transaction);

    List<UserTransaction> toUserTransactionList(List<Transaction> transactions);
}
