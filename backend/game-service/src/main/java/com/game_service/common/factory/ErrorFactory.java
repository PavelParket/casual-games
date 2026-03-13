package com.game_service.common.factory;

import com.game_service.common.dto.ErrorResponse;
import com.game_service.common.enums.ErrorCode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
public class ErrorFactory {

    public ErrorResponse create(ErrorCode errorCode, String message, HttpStatus status) {
        return base(errorCode, message, status).build();
    }

    public ErrorResponse create(ErrorCode errorCode, String message, HttpStatus status, String path) {
        return base(errorCode, message, status)
                .path(path)
                .build();
    }

    public ErrorResponse create(ErrorCode errorCode, String message, HttpStatus status, Map<String, List<String>> details) {
        return base(errorCode, message, status)
                .details(details)
                .build();
    }

    /**
     * Используется для cooldown-ответов, где заголовок Retry-After прикрепляется вызывающим кодом отдельно.
     * Тело ответа идентично базовой перегрузке — заголовки не являются частью тела.
     *
     * <p>Перегрузка добавлена для обратной совместимости с текущим {@code GlobalExceptionHandler},
     * который формирует {@code HttpHeaders} и передаёт их сюда.
     *
     * <p>TODO: избавиться от этой перегрузки после рефакторинга обработки cooldown.
     * Шаги миграции:
     * <ol>
     *   <li>В {@code GlobalExceptionHandler.handleCooldown()} убрать создание {@code HttpHeaders} и вызов этого метода.</li>
     *   <li>Сделать так, чтобы хендлер возвращал {@code ResponseEntity<ErrorResponse>} вместо {@code ErrorResponse},
     *       что позволит прикрепить заголовок {@code Retry-After} непосредственно к ответу:
     *       {@code ResponseEntity.status(429).header(RETRY_AFTER, ...).body(factory.create(...))}.</li>
     *   <li>Убрать аннотацию {@code @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)} с метода хендлера —
     *       статус будет задаваться через {@code ResponseEntity}.</li>
     *   <li>Удалить эту перегрузку из {@code ErrorFactory}.</li>
     * </ol>
     */
    public ErrorResponse create(ErrorCode errorCode, String message, HttpHeaders headers, HttpStatus status) {
        return base(errorCode, message, status).build();
    }

    private ErrorResponse.ErrorResponseBuilder base(ErrorCode errorCode, String message, HttpStatus status) {
        return ErrorResponse.builder()
                .errorCode(errorCode)
                .message(message)
                .status(status.value())
                .timestamp(Instant.now());
    }
}
