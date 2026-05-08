package com.bank_service.service.grpc;

import com.bank_service.service.GameTransactionService;
import com.casualgames.grpc.transaction.DeCoderTransactionRequest;
import com.casualgames.grpc.transaction.DurakTransactionRequest;
import com.casualgames.grpc.transaction.GameTransactionResponse;
import com.casualgames.grpc.transaction.GameTransactionServiceGrpc;
import com.casualgames.grpc.transaction.HorseRaceTransactionRequest;
import com.casualgames.grpc.transaction.TicTacToeTransactionRequest;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.function.Supplier;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class GrpcGameTransactionService extends GameTransactionServiceGrpc.GameTransactionServiceImplBase {

    private final GameTransactionService gameTransactionService;

    @Override
    public void processTicTacToeTransaction(TicTacToeTransactionRequest request,
                                            StreamObserver<GameTransactionResponse> responseObserver) {
        log.info("gRPC ProcessTicTacToeTransaction: roomId={}", request.getRoomId());

        handle(() -> gameTransactionService.processTicTacToe(request), responseObserver);
    }

    @Override
    public void processHorseRaceTransaction(HorseRaceTransactionRequest request,
                                            StreamObserver<GameTransactionResponse> responseObserver) {
        log.info("gRPC ProcessHorseRaceTransaction: roomId={}", request.getRoomId());

        handle(() -> gameTransactionService.processHorseRace(request), responseObserver);
    }

    @Override
    public void processDurakTransaction(DurakTransactionRequest request,
                                        StreamObserver<GameTransactionResponse> responseObserver) {
        log.info("gRPC ProcessDurakTransaction: roomId={}", request.getRoomId());

        handle(() -> gameTransactionService.processDurak(request), responseObserver);
    }

    @Override
    public void processDeCoderTransaction(DeCoderTransactionRequest request,
                                          StreamObserver<GameTransactionResponse> responseObserver) {
        log.info("gRPC ProcessDeCoderTransaction: roomId={}", request.getRoomId());

        handle(() -> gameTransactionService.processDeCoder(request), responseObserver);
    }

    private void handle(Supplier<GameTransactionResponse> supplier,
                        StreamObserver<GameTransactionResponse> responseObserver) {
        responseObserver.onNext(supplier.get());
        responseObserver.onCompleted();
    }
}
