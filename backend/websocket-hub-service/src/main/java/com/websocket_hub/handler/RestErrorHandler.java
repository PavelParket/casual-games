package com.websocket_hub.handler;

import com.common_utils.exception.ServiceUnavailableException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.websocket_hub.domain.dto.ErrorResponse;
import com.websocket_hub.domain.enums.ErrorCode;
import com.websocket_hub.exception.GameException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;
import java.net.URI;

@Component
@RequiredArgsConstructor
@Slf4j
public class RestErrorHandler implements ResponseErrorHandler {

    private final ObjectMapper objectMapper;

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().isError();
    }

    @Override
    public void handleError(@NonNull URI url, @NonNull HttpMethod method, ClientHttpResponse response) throws IOException {
        int statusCode = response.getStatusCode().value();
        String body = new String(response.getBody().readAllBytes());

        ErrorResponse errorResponse = tryParse(body);

        if (errorResponse == null) {
            log.warn("Could not parse downstream error body (status={}): {}", statusCode, body);
            throw new ServiceUnavailableException(ErrorCode.SERVICE_UNAVAILABLE.getMessage());
        }

        ErrorCode errorCode = resolveErrorCode(errorResponse, statusCode);
        String clientMessage = resolveMessage(errorResponse, errorCode);

        throw new GameException(errorCode, clientMessage);
    }

    private ErrorResponse tryParse(String body) {
        try {
            return objectMapper.readValue(body, ErrorResponse.class);
        } catch (Exception e) {
            return null;
        }
    }

    private ErrorCode resolveErrorCode(ErrorResponse response, int httpStatus) {
        if (response.errorCode() != null) {
            try {
                return response.errorCode();
            } catch (IllegalArgumentException e) {
                log.warn("Unknown downstream errorCode '{}', falling back to HTTP status {}", response.errorCode(), httpStatus);
            }
        }

        return httpStatus >= 500 ? ErrorCode.INTERNAL_SERVER_ERROR : ErrorCode.BAD_REQUEST;
    }

    private String resolveMessage(ErrorResponse response, ErrorCode errorCode) {
        return switch (errorCode) {
            case UNAUTHORIZED, FORBIDDEN, NOT_FOUND, BAD_REQUEST, CONFLICT,
                 INVALID_MESSAGE, INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE -> errorCode.getMessage();
            default -> response.message();
        };
    }
}
