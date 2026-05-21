package com.bank_service.service.grpc;

import com.bank_service.domain.entity.Transaction;
import com.bank_service.service.SubscriptionTransactionService;
import com.casualgames.grpc.subscription.SubscriptionTransactionRequest;
import com.casualgames.grpc.subscription.SubscriptionTransactionResponse;
import com.casualgames.grpc.subscription.SubscriptionTransactionServiceGrpc;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class GrpcSubscriptionTransactionService extends SubscriptionTransactionServiceGrpc.SubscriptionTransactionServiceImplBase {

    private final SubscriptionTransactionService subscriptionTransactionService;

    @Override
    public void charge(SubscriptionTransactionRequest request,
                       StreamObserver<SubscriptionTransactionResponse> responseObserver) {
        log.info("gRPC Charge subscription: userGuid={}, type={}", request.getUserGuid(), request.getType());

        Transaction transaction = subscriptionTransactionService.processSubscription(request);

        SubscriptionTransactionResponse response = SubscriptionTransactionResponse.newBuilder()
                .setTransactionId(transaction.getId())
                .setStatus(transaction.getStatus().name())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
