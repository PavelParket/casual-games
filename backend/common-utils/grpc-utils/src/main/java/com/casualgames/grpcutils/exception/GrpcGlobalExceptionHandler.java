package com.casualgames.grpcutils.exception;

import com.casualgames.commonutils.exception.BadRequestException;
import com.casualgames.commonutils.exception.ConflictException;
import com.casualgames.commonutils.exception.ForbiddenException;
import com.casualgames.commonutils.exception.NotFoundException;
import com.casualgames.commonutils.exception.ServiceUnavailableException;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

@GrpcAdvice
@ConditionalOnClass(GrpcAdvice.class)
@RequiredArgsConstructor
@Slf4j
public class GrpcGlobalExceptionHandler {

    private static final String INTERNAL_SERVER_ERROR = "Unexpected server error. Please try again";

    @GrpcExceptionHandler(BadRequestException.class)
    public StatusRuntimeException handleBadRequest(BadRequestException e) {
        log.warn("gRPC bad request: {}", e.getMessage());
        return Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException();
    }

    @GrpcExceptionHandler(ForbiddenException.class)
    public StatusRuntimeException handleForbidden(ForbiddenException e) {
        log.warn("gRPC forbidden: {}", e.getMessage());
        return Status.PERMISSION_DENIED.withDescription(e.getMessage()).asRuntimeException();
    }

    @GrpcExceptionHandler(NotFoundException.class)
    public StatusRuntimeException handleNotFound(NotFoundException e) {
        log.warn("gRPC not found: {}", e.getMessage());
        return Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException();
    }

    @GrpcExceptionHandler(ConflictException.class)
    public StatusRuntimeException handleConflict(ConflictException e) {
        log.warn("gRPC conflict: {}", e.getMessage());
        return Status.ALREADY_EXISTS.withDescription(e.getMessage()).asRuntimeException();
    }

    @GrpcExceptionHandler(ServiceUnavailableException.class)
    public StatusRuntimeException handleServiceUnavailable(ServiceUnavailableException e) {
        log.error("gRPC service unavailable: {}", e.getMessage());
        return Status.UNAVAILABLE.withDescription(e.getMessage()).asRuntimeException();
    }

    @GrpcExceptionHandler(Exception.class)
    public StatusRuntimeException handleGeneral(Exception e) {
        log.error("gRPC unexpected error", e);
        return Status.INTERNAL.withDescription(INTERNAL_SERVER_ERROR).asRuntimeException();
    }
}
