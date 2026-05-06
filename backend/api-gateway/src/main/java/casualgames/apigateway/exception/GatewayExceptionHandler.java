package casualgames.apigateway.exception;

import com.common_utils.dto.ErrorResponse;
import com.common_utils.enums.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Order(-2)
@RequiredArgsConstructor
@Slf4j
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;


    @Override
    @NonNull
    public Mono<Void> handle(@NonNull ServerWebExchange exchange, @NonNull Throwable ex) {
        HttpStatus status = resolveStatus(ex);
        ErrorCode errorCode = resolveErrorCode(status);
        String path = exchange.getRequest().getURI().getPath();

        if (status == HttpStatus.INTERNAL_SERVER_ERROR) {
            log.error("Unhandled gateway exception on path={}: {}", path, ex.getMessage(), ex);
        } else {
            log.warn("Gateway exception on path={}, status={}: {}", path, status, ex.getMessage());
        }

        return writeResponse(exchange, status, errorCode, path);
    }

    private HttpStatus resolveStatus(Throwable ex) {
        if (ex instanceof ResponseStatusException e) {
            HttpStatus resolved = HttpStatus.resolve(e.getStatusCode().value());

            return resolved != null ? resolved : HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private ErrorCode resolveErrorCode(HttpStatus status) {
        return switch (status) {
            case NOT_FOUND -> ErrorCode.NOT_FOUND;
            case TOO_MANY_REQUESTS -> ErrorCode.TOO_MANY_REQUESTS;
            case SERVICE_UNAVAILABLE -> ErrorCode.SERVICE_UNAVAILABLE;
            default -> ErrorCode.INTERNAL_SERVER_ERROR;
        };
    }

    private Mono<Void> writeResponse(ServerWebExchange exchange,
                                     HttpStatus status,
                                     ErrorCode errorCode,
                                     String path) {
        try {
            ErrorResponse response = ErrorResponse.of(
                    errorCode,
                    status,
                    errorCode.getMessage(),
                    null,
                    path
            );

            byte[] bytes = objectMapper.writeValueAsBytes(response);
            DataBuffer dataBuffer = exchange.getResponse().bufferFactory().wrap(bytes);

            exchange.getResponse().setStatusCode(status);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

            return exchange.getResponse().writeWith(Mono.just(dataBuffer));
        } catch (Exception e) {
            log.error("Failed to write error response", e);

            exchange.getResponse().setStatusCode(status);

            return exchange.getResponse().setComplete();
        }
    }
}
