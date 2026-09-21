package com.casualgames.commonutils.exception;

import com.casualgames.commonutils.dto.ErrorResponse;
import com.casualgames.commonutils.enums.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.casualgames.commonutils.config.ResourceMessageConstants.VALIDATION_FAILED;
import static org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.SERVLET;

@ConditionalOnWebApplication(type = SERVLET)
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(AbstractException.class)
    public ResponseEntity<ErrorResponse> handleException(AbstractException ex, HttpServletRequest request) {
        HttpStatus status = resolveDeclaredStatus(ex);

        log(status, ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.fromStatus(status), ex.getMessage(), request.getRequestURI(), null);

        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex, HttpServletRequest request) {
        log(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR, ex.getMessage(), request.getRequestURI(), null);

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  @NonNull HttpHeaders headers,
                                                                  @NonNull HttpStatusCode status,
                                                                  @NonNull WebRequest request) {
        Map<String, List<String>> details = ex.getBindingResult().getFieldErrors()
                .stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        Collectors.mapping(
                                FieldError::getDefaultMessage,
                                Collectors.toList()
                        )
                ));

        String message = details.values()
                .stream()
                .flatMap(List::stream)
                .findFirst()
                .orElse(VALIDATION_FAILED);

        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.BAD_REQUEST, message, getPath(request), details);

        return handleExceptionInternal(ex, errorResponse, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(@NonNull Exception ex,
                                                             Object body,
                                                             @NonNull HttpHeaders headers,
                                                             HttpStatusCode statusCode,
                                                             @NonNull WebRequest request) {
        HttpStatus status = HttpStatus.valueOf(statusCode.value());

        log(status, ex.getMessage(), ex);

        ErrorResponse errorResponse = body instanceof ErrorResponse response
                ? response
                : ErrorResponse.of(ErrorCode.fromStatus(status), ex.getMessage(), getPath(request), null);

        return new ResponseEntity<>(errorResponse, headers, status);
    }

    private HttpStatus resolveDeclaredStatus(Exception ex) {
        ResponseStatus annotation = AnnotatedElementUtils.findMergedAnnotation(ex.getClass(), ResponseStatus.class);
        return annotation != null ? annotation.value() : HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String getPath(WebRequest request) {
        return ((ServletWebRequest) request).getRequest().getRequestURI();
    }

    private void log(HttpStatus status, String message, Exception ex) {
        if (status.is5xxServerError()) {
            log.error("Server error on: {}", message, ex);
        } else {
            log.warn("Client error on: {}", message, ex);
        }
    }

}
