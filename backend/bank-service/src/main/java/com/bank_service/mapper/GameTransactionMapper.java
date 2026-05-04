package com.bank_service.mapper;

import com.bank_service.domain.dto.game.GameTransactionRequest;
import com.bank_service.domain.entity.HorseRacePlayerBet;
import com.bank_service.domain.entity.PlayerBet;
import com.casualgames.grpc.transaction.GameTransactionResponse;
import com.casualgames.grpc.transaction.HorseRacePlayerBetMessage;
import com.casualgames.grpc.transaction.PlayerBetMessage;
import com.grpc_utils.mapper.GrpcTimestampMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", imports = Instant.class)
public interface GameTransactionMapper {

    @Mapping(target = "roomId", expression = "java(request.roomId())")
    @Mapping(target = "roomType", expression = "java(request.roomType())")
    @Mapping(target = "processedAt", expression = "java(Instant.now())")
    com.bank_service.domain.dto.game.GameTransactionResponse toResponse(GameTransactionRequest request, int transactionCount);

    default GameTransactionResponse toGrpcResponse(GameTransactionRequest request, int transactionCount) {
        return GameTransactionResponse.newBuilder()
                .setRoomId(request.roomId().toString())
                .setRoomType(request.roomType().name())
                .setTransactionCount(transactionCount)
                .setProcessedAt(GrpcTimestampMapper.toTimestamp(Instant.now()))
                .build();
    }

    @Mapping(target = "guid", source = "guid", qualifiedByName = "mapToUUID")
    @Mapping(target = "bet", source = "bet", qualifiedByName = "mapToBigDecimal")
    @Mapping(target = "balanceBefore", source = "balanceBefore", qualifiedByName = "mapToBigDecimal")
    PlayerBet toPlayerBet(PlayerBetMessage message);

    List<PlayerBet> toPlayerBetList(List<PlayerBetMessage> messages);

    @Mapping(target = "guid", source = "guid", qualifiedByName = "mapToUUID")
    @Mapping(target = "amount", source = "amount", qualifiedByName = "mapToBigDecimal")
    @Mapping(target = "balanceBefore", source = "balanceBefore", qualifiedByName = "mapToBigDecimal")
    HorseRacePlayerBet toHorseRacePlayerBet(HorseRacePlayerBetMessage message);

    List<HorseRacePlayerBet> toHorseRacePlayerBetList(List<HorseRacePlayerBetMessage> messages);

    @Named("mapToBigDecimal")
    default BigDecimal mapToBigDecimal(String value) {
        return new BigDecimal(value);
    }

    @Named("mapToUUID")
    default UUID mapToUUID(String value) {
        return UUID.fromString(value);
    }
}
