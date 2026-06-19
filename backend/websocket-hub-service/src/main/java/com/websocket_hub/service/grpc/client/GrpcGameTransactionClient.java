package com.websocket_hub.service.grpc.client;

import com.casualgames.grpc.transaction.DeCoderTransactionRequest;
import com.casualgames.grpc.transaction.DurakTransactionRequest;
import com.casualgames.grpc.transaction.GameTransactionResponse;
import com.casualgames.grpc.transaction.GameTransactionServiceGrpc;
import com.casualgames.grpc.transaction.HorseRaceTransactionRequest;
import com.casualgames.grpc.transaction.TicTacToeTransactionRequest;
import com.grpc_utils.mapper.GrpcStatusExceptionMapper;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class GrpcGameTransactionClient {

    @GrpcClient("bank-service")
    private GameTransactionServiceGrpc.GameTransactionServiceBlockingStub gameTransactionServiceBlockingStub;

    private final GrpcStatusExceptionMapper grpcStatusExceptionMapper;

    public GameTransactionResponse saveTicTacToeGameResults(TicTacToeTransactionRequest request) {
        log.info("gRPC process Tic-Tac-Toe transaction: roomId={}, winner={}", request.getRoomId(), request.getWinner());

        return execute(request, gameTransactionServiceBlockingStub::processTicTacToeTransaction);
    }

    public GameTransactionResponse saveHorseRaceGameResults(HorseRaceTransactionRequest request) {
        log.info("gRPC process Horse Race transaction: roomId={}, winner horse={}", request.getRoomId(), request.getWinnerHorseIndex());

        return execute(request, gameTransactionServiceBlockingStub::processHorseRaceTransaction);
    }

    public GameTransactionResponse saveDurakGameResults(DurakTransactionRequest request) {
        log.info("gRPC process Durak transaction: roomId={}, winner={}", request.getRoomId(), request.getWinner());

        return execute(request, gameTransactionServiceBlockingStub::processDurakTransaction);
    }

    public GameTransactionResponse saveDeCoderGameResults(DeCoderTransactionRequest request) {
        log.info("gRPC process De-Coder transaction: roomId={}, playerCount={}", request.getRoomId(), request.getPlayerTransactionsCount());

        return execute(request, gameTransactionServiceBlockingStub::processDeCoderTransaction);
    }

    private <T> GameTransactionResponse execute(T request, Function<T, GameTransactionResponse> function) {
        try {
            return function.apply(request);
        } catch (StatusRuntimeException e) {
            log.error("gRPC bank call failed: status={}, description={}", e.getStatus().getCode(), e.getStatus().getDescription(), e);

            throw grpcStatusExceptionMapper.toException(e);
        }
    }
}
