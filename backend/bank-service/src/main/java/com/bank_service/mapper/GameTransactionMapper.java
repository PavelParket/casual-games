package com.bank_service.mapper;

import com.bank_service.domain.dto.GameTransactionRequest;
import com.bank_service.domain.dto.GameTransactionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GameTransactionMapper {

    @Mapping(target = "roomId", expression = "java(request.roomId())")
    @Mapping(target = "roomType", expression = "java(request.roomType())")
    @Mapping(target = "processedAt", expression = "java(Instant.now())")
    GameTransactionResponse toResponse(GameTransactionRequest request, int transactionCount);
}
