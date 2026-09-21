package com.casualgames.filemanagementstarter.exception;

import com.casualgames.commonutils.dto.ErrorResponse;
import com.casualgames.commonutils.enums.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import static com.casualgames.filemanagementstarter.config.ResourceMessageConstants.NOT_FOUND_RESOURCE_IN_STORAGE;
import static org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.SERVLET;

@ConditionalOnWebApplication(type = SERVLET)
@RestControllerAdvice
@ConditionalOnClass(S3Client.class)
@Slf4j
public class S3ExceptionHandler {

    @ExceptionHandler(NoSuchKeyException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNoSuchKeyException(NoSuchKeyException ex, HttpServletRequest request) {
        log.warn("S3 object not found: {}", ex.getMessage());

        return ErrorResponse.of(ErrorCode.NOT_FOUND, NOT_FOUND_RESOURCE_IN_STORAGE, request.getRequestURI(), null);
    }

    @ExceptionHandler(S3OperationException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ErrorResponse handleS3OperationException(S3OperationException ex, HttpServletRequest request) {
        log.error("S3 operation failed: {}", ex.getMessage(), ex);

        return ErrorResponse.of(ErrorCode.SERVICE_UNAVAILABLE, ErrorCode.SERVICE_UNAVAILABLE.getMessage(), request.getRequestURI(), null);
    }
}
