package com.ecommerce.gateway.exception;

import com.ecommerce.common.dto.ErrorResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.time.Instant;
import java.util.concurrent.TimeoutException;

@Component
@Order(-2)
@RequiredArgsConstructor
@Slf4j
public class GatewayErrorExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = "An unexpected error occurred on the API Gateway";

        if (ex instanceof ResponseStatusException rse) {
            status = HttpStatus.valueOf(rse.getStatusCode().value());
            message = rse.getReason() != null ? rse.getReason() : rse.getMessage();
        } else if (ex instanceof TimeoutException
                || ex.getCause() instanceof TimeoutException
                || ex.getClass().getName().contains("TimeoutException")
                || (ex.getCause() != null && ex.getCause().getClass().getName().contains("TimeoutException"))) {
            status = HttpStatus.GATEWAY_TIMEOUT;
            message = "Gateway Timeout: Downstream service call timed out";
        } else if (ex instanceof ConnectException
                || ex.getCause() instanceof ConnectException
                || ex.getClass().getName().contains("ConnectException")
                || (ex.getCause() != null && ex.getCause().getClass().getName().contains("ConnectException"))) {
            status = HttpStatus.BAD_GATEWAY;
            message = "Bad Gateway: Downstream service is currently unreachable";
        } else {
            message = ex.getMessage();
        }

        log.error("API Gateway Error: [Status: {}] [Exception: {}] [Message: {}]", status, ex.getClass().getName(), message);

        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .timestamp(Instant.now())
                .build();

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(errorResponse);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize error response", e);
            bytes = "{\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"Failed to serialize error response\"}".getBytes();
        }

        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}
