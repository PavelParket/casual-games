package com.casualgames.bankservice.mapper;

import com.casualgames.grpc.transaction.GameTransactionResponse;
import com.casualgames.grpcutils.mapper.GrpcTimestampMapper;
import org.mapstruct.Mapper;

import java.time.Instant;

@Mapper(componentModel = "spring", imports = Instant.class)
public interface GameTransactionMapper {

    default GameTransactionResponse toGrpcResponse(String roomId, String roomType, int transactionCount) {
        return GameTransactionResponse.newBuilder()
                .setRoomId(roomId)
                .setRoomType(roomType)
                .setTransactionCount(transactionCount)
                .setProcessedAt(GrpcTimestampMapper.toTimestamp(Instant.now()))
                .build();
    }
}
