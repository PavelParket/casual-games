package com.bank_service.mapper;

import com.bank_service.domain.dto.game.GameTransactionRequest;
import com.bank_service.domain.dto.game.GameTransactionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;

@Mapper(componentModel = "spring", imports = Instant.class)
public interface GameTransactionMapper {

    @Mapping(target = "roomId", expression = "java(request.roomId())")
    @Mapping(target = "roomType", expression = "java(request.roomType())")
    @Mapping(target = "processedAt", expression = "java(Instant.now())")
    GameTransactionResponse toResponse(GameTransactionRequest request, int transactionCount);
}
