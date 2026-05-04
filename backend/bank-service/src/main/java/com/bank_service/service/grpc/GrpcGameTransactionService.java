package com.bank_service.service.grpc;

import com.bank_service.mapper.GameTransactionMapper;
import com.bank_service.service.BankService;
import com.casualgames.grpc.transaction.GameTransactionServiceGrpc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class GrpcGameTransactionService extends GameTransactionServiceGrpc.GameTransactionServiceImplBase {

    private final BankService bankService;

    private final GameTransactionMapper gameTransactionMapper;


}
