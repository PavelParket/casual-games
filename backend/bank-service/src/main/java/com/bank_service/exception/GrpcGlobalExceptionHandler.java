package com.bank_service.exception;

import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

//@GrpcAdvice
@RequiredArgsConstructor
@Slf4j
public class GrpcGlobalExceptionHandler {

    /*@GrpcExceptionHandler(BadRequestException.class)
    public StatusRuntimeException handleBadRequest(BadRequestException e) {
        log.warn("gRPC bad request: {}", e.getMessage());
        return Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException();
    }

    @GrpcExceptionHandler(NotFoundException.class)
    public StatusRuntimeException handleNotFound(ResourceNotFoundException e) {
        log.warn("gRPC not found: {}", e.getMessage());
        return Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException();
    }

    @GrpcExceptionHandler(ConflictException.class)
    public StatusRuntimeException handleConflict(ConflictException e) {
        log.warn("gRPC conflict: {}", e.getMessage());
        return Status.ALREADY_EXISTS.withDescription(e.getMessage()).asRuntimeException();
    }*/

    /*@GrpcExceptionHandler(Exception.class)
    public StatusRuntimeException handleGeneral(Exception e) {
        log.error("gRPC unexpected error", e);
        return Status.INTERNAL.withDescription("Internal server error").asRuntimeException();
    }*/

    public static ServiceUnavailableException mapToServiceException(StatusRuntimeException e) {
        String description = e.getStatus().getDescription() != null
                ? e.getStatus().getDescription()
                : "Unknown gRPC error";

        return switch (e.getStatus().getCode()) {
            case ALREADY_EXISTS, INVALID_ARGUMENT -> new ServiceUnavailableException(description);

            case UNAVAILABLE -> new ServiceUnavailableException("User service is unavailable");

            default -> new ServiceUnavailableException("Failed to create user: " + description);
        };
    }
}
