package casualgames.userservice.service.grpc.client;

import com.casualgames.grpc.subscription.SubscriptionTransactionRequest;
import com.casualgames.grpc.subscription.SubscriptionTransactionResponse;
import com.casualgames.grpc.subscription.SubscriptionTransactionServiceGrpc;
import com.grpc_utils.mapper.GrpcStatusExceptionMapper;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GrpcBankClient {

    @GrpcClient("bank-service")
    private SubscriptionTransactionServiceGrpc.SubscriptionTransactionServiceBlockingStub subscriptionTransactionServiceBlockingStub;

    private final GrpcStatusExceptionMapper grpcStatusExceptionMapper;

    public SubscriptionTransactionResponse charge(SubscriptionTransactionRequest request) {
        try {
            return subscriptionTransactionServiceBlockingStub.charge(request);
        } catch (StatusRuntimeException e) {
            log.error("gRPC Charge failed: status={}, description={}", e.getStatus().getCode(), e.getStatus().getDescription(), e);
            throw grpcStatusExceptionMapper.toException(e);
        }
    }
}
