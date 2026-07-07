package com.ecommerce.gateway.exception;

import com.ecommerce.common.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ResponseStatusException;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.net.ConnectException;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

public class GatewayErrorExceptionHandlerTest {

    private GatewayErrorExceptionHandler handler;
    private ObjectMapper objectMapper;

    // Custom exceptions to test string-matching logic
    private static class DummyTimeoutException extends RuntimeException {
        public DummyTimeoutException(String message) {
            super(message);
        }
    }

    private static class DummyConnectException extends RuntimeException {
        public DummyConnectException(String message) {
            super(message);
        }
    }

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        handler = new GatewayErrorExceptionHandler(objectMapper);
    }

    private ErrorResponse parseResponse(MockServerWebExchange exchange) throws IOException {
        String body = exchange.getResponse().getBodyAsString().block();
        return objectMapper.readValue(body, ErrorResponse.class);
    }

    @Test
    public void testHandle_ResponseStatusException() throws IOException {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found");

        StepVerifier.create(handler.handle(exchange, ex))
                .verifyComplete();

        assertEquals(HttpStatus.NOT_FOUND, exchange.getResponse().getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, exchange.getResponse().getHeaders().getContentType());

        ErrorResponse res = parseResponse(exchange);
        assertEquals(404, res.getStatus());
        assertEquals("Not Found", res.getError());
        assertTrue(res.getMessage().contains("Resource not found"));
        assertNotNull(res.getTimestamp());
    }

    @Test
    public void testHandle_TimeoutException() throws IOException {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());
        TimeoutException ex = new TimeoutException("Connection timed out");

        StepVerifier.create(handler.handle(exchange, ex))
                .verifyComplete();

        assertEquals(HttpStatus.GATEWAY_TIMEOUT, exchange.getResponse().getStatusCode());

        ErrorResponse res = parseResponse(exchange);
        assertEquals(504, res.getStatus());
        assertEquals("Gateway Timeout", res.getError());
        assertEquals("Gateway Timeout: Downstream service call timed out", res.getMessage());
    }

    @Test
    public void testHandle_NettyTimeoutException_StringMatching() throws IOException {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());
        DummyTimeoutException ex = new DummyTimeoutException("Netty read timeout");

        StepVerifier.create(handler.handle(exchange, ex))
                .verifyComplete();

        assertEquals(HttpStatus.GATEWAY_TIMEOUT, exchange.getResponse().getStatusCode());

        ErrorResponse res = parseResponse(exchange);
        assertEquals(504, res.getStatus());
        assertEquals("Gateway Timeout", res.getError());
    }

    @Test
    public void testHandle_WrappedTimeoutException() throws IOException {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());
        RuntimeException ex = new RuntimeException(new TimeoutException("Inner timeout"));

        StepVerifier.create(handler.handle(exchange, ex))
                .verifyComplete();

        assertEquals(HttpStatus.GATEWAY_TIMEOUT, exchange.getResponse().getStatusCode());
    }

    @Test
    public void testHandle_ConnectException() throws IOException {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());
        ConnectException ex = new ConnectException("Connection refused");

        StepVerifier.create(handler.handle(exchange, ex))
                .verifyComplete();

        assertEquals(HttpStatus.BAD_GATEWAY, exchange.getResponse().getStatusCode());

        ErrorResponse res = parseResponse(exchange);
        assertEquals(502, res.getStatus());
        assertEquals("Bad Gateway", res.getError());
        assertEquals("Bad Gateway: Downstream service is currently unreachable", res.getMessage());
    }

    @Test
    public void testHandle_NettyConnectException_StringMatching() throws IOException {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());
        DummyConnectException ex = new DummyConnectException("Netty connect error");

        StepVerifier.create(handler.handle(exchange, ex))
                .verifyComplete();

        assertEquals(HttpStatus.BAD_GATEWAY, exchange.getResponse().getStatusCode());

        ErrorResponse res = parseResponse(exchange);
        assertEquals(502, res.getStatus());
        assertEquals("Bad Gateway", res.getError());
    }

    @Test
    public void testHandle_GenericException() throws IOException {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());
        RuntimeException ex = new RuntimeException("Generic app error");

        StepVerifier.create(handler.handle(exchange, ex))
                .verifyComplete();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exchange.getResponse().getStatusCode());

        ErrorResponse res = parseResponse(exchange);
        assertEquals(500, res.getStatus());
        assertEquals("Internal Server Error", res.getError());
        assertEquals("Generic app error", res.getMessage());
    }
}
