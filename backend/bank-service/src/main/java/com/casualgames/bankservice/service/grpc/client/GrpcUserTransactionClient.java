package com.casualgames.bankservice.service.grpc.client;

import com.casualgames.bankservice.domain.entity.Transaction;
import com.casualgames.bankservice.mapper.TransactionMapper;
import com.casualgames.grpc.transaction.UpdateBalancesRequest;
import com.casualgames.grpc.transaction.UserTransactionServiceGrpc;
import com.casualgames.grpcutils.mapper.GrpcStatusExceptionMapper;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GrpcUserTransactionClient {

    @GrpcClient("user-service")
    private UserTransactionServiceGrpc.UserTransactionServiceBlockingStub userTransactionServiceBlockingStub;

    private final TransactionMapper transactionMapper;

    private final GrpcStatusExceptionMapper grpcStatusExceptionMapper;

    public void sendUpdates(List<Transaction> transactions) {
        try {
            UpdateBalancesRequest request = UpdateBalancesRequest.newBuilder()
                    .addAllTransactions(transactionMapper.toUserTransactionList(transactions))
                    .build();

            userTransactionServiceBlockingStub.updateBalances(request);
        } catch (StatusRuntimeException e) {
            log.error("gRPC call to user-service failed: status={}, description={}", e.getStatus().getCode(), e.getStatus().getDescription(), e);
            throw grpcStatusExceptionMapper.toException(e);
        }
    }
}
