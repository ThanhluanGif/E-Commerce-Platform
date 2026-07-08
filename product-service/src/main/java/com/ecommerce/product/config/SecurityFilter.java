package com.ecommerce.product.config;

import com.ecommerce.common.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class SecurityFilter implements Filter {

    private final ObjectMapper objectMapper;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String method = httpRequest.getMethod();

        // Enforce role check for state-modifying requests (POST, PUT, DELETE)
        if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method)) {
            String rolesHeader = httpRequest.getHeader("X-User-Roles");
            boolean isAdmin = false;

            if (rolesHeader != null && !rolesHeader.isBlank()) {
                isAdmin = Arrays.stream(rolesHeader.split(","))
                        .map(String::trim)
                        .anyMatch(role -> "ROLE_ADMIN".equalsIgnoreCase(role));
            }

            if (!isAdmin) {
                httpResponse.setStatus(HttpStatus.FORBIDDEN.value());
                httpResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);

                ErrorResponse errorResponse = ErrorResponse.builder()
                        .status(HttpStatus.FORBIDDEN.value())
                        .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                        .message("Access denied: Admin role required for this operation")
                        .timestamp(Instant.now())
                        .build();

                httpResponse.getWriter().write(objectMapper.writeValueAsString(errorResponse));
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
