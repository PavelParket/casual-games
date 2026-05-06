package com.grpc_utils.mapper;

import com.common_utils.exception.AbstractException;
import com.common_utils.exception.BadRequestException;
import com.common_utils.exception.ConflictException;
import com.common_utils.exception.ForbiddenException;
import com.common_utils.exception.NotFoundException;
import com.common_utils.exception.ServiceUnavailableException;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GrpcStatusExceptionMapper {

    private static final String SERVICE_UNAVAILABLE = "Service is unavailable now. Please try later";

    public AbstractException toException(StatusRuntimeException e) {
        String description = e.getStatus().getDescription() != null
                ? e.getStatus().getDescription()
                : e.getStatus().getCode().name();

        return switch (e.getStatus().getCode()) {
            case NOT_FOUND -> new NotFoundException(description);
            case ALREADY_EXISTS -> new ConflictException(description);
            case INVALID_ARGUMENT -> new BadRequestException(description);
            case PERMISSION_DENIED -> new ForbiddenException(description);
            default -> new ServiceUnavailableException(SERVICE_UNAVAILABLE);
        };
    }
}
