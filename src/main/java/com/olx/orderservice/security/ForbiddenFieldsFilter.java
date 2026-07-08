package com.olx.orderservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Servlet filter that inspects incoming POST, PUT, and PATCH request bodies
 * for forbidden JSON property keys that must never be supplied by the client.
 * orderId, status, and totalAmount are server managed fields.
 * If any are detected, a 400 Bad Request is returned immediately.
 * Uses the wrapped request pattern so the body remains available downstream.
 */
@Component
@Order(1)
public class ForbiddenFieldsFilter extends HttpFilter {

    private static final Set<String> FORBIDDEN_KEYS = Set.of(
        "\"orderId\"",
        "\"status\"",
        "\"totalAmount\""
    );

    // Matches only JSON property keys (not values in string content)
    private static final Pattern FORBIDDEN_PATTERN = Pattern.compile(
        "(" + String.join("|", FORBIDDEN_KEYS) + ")\\s*:"
    );

    @Override
    protected void doFilter(HttpServletRequest request,
                            HttpServletResponse response,
                            FilterChain chain) throws IOException, ServletException {
        String method = request.getMethod();
        String path = request.getRequestURI();

        // Exclude status transition endpoint where "status" is a legitimate field
        boolean isStatusEndpoint = path != null && path.endsWith("/status");

        if (!isStatusEndpoint
            && ("POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method))) {
            BufferedServletWrapper wrappedRequest = new BufferedServletWrapper(request);
            String body = wrappedRequest.getBody();

            if (body != null && !body.isBlank()
                && FORBIDDEN_PATTERN.matcher(body).find()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType("application/json");
                response.getWriter().write(String.format(
                    "{\"status\":400,\"error\":\"Bad Request\"," +
                    "\"message\":\"Forbidden fields detected. orderId, status, " +
                    "and totalAmount are server managed and must not be provided\"," +
                    "\"timestamp\":\"%s\"," +
                    "\"path\":\"%s\"}",
                    Instant.now(), path
                ));
                return;
            }

            chain.doFilter(wrappedRequest, response);
        } else {
            chain.doFilter(request, response);
        }
    }
}
