package com.olx.orderservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Tests for ForbiddenFieldsFilter.
 * Verifies that requests containing server managed fields orderId, status,
 * and totalAmount are rejected with 400 Bad Request.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ForbiddenFieldsFilterTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Test
    void shouldRejectCreateRequestWithOrderId() throws Exception {
        String body = "{\"customerName\":\"Andi\",\"items\":[" +
            "{\"productName\":\"Apple\",\"quantity\":1,\"unitPrice\":1.00}]," +
            "\"orderId\":\"" + UUID.randomUUID() + "\"}";

        HttpResponse<String> response = sendPost("/api/orders", body);
        assertEquals(400, response.statusCode());
    }

    @Test
    void shouldRejectCreateRequestWithStatus() throws Exception {
        String body = "{\"customerName\":\"Andi\",\"items\":[" +
            "{\"productName\":\"Apple\",\"quantity\":1,\"unitPrice\":1.00}]," +
            "\"status\":\"PAID\"}";

        HttpResponse<String> response = sendPost("/api/orders", body);
        assertEquals(400, response.statusCode());
    }

    @Test
    void shouldRejectCreateRequestWithTotalAmount() throws Exception {
        String body = "{\"customerName\":\"Andi\",\"items\":[" +
            "{\"productName\":\"Apple\",\"quantity\":1,\"unitPrice\":1.00}]," +
            "\"totalAmount\":0.50}";

        HttpResponse<String> response = sendPost("/api/orders", body);
        assertEquals(400, response.statusCode());
    }

    @Test
    void shouldRejectUpdateRequestWithTotalAmount() throws Exception {
        String body = "{\"customerName\":\"Budi\",\"items\":[" +
            "{\"productName\":\"Apple\",\"quantity\":1,\"unitPrice\":1.00}]," +
            "\"totalAmount\":0.01}";

        HttpResponse<String> response = sendPut("/api/orders/" + UUID.randomUUID(), body);
        assertEquals(400, response.statusCode());
    }

    @Test
    void shouldAllowLegitimateStatusTransition() throws Exception {
        String body = "{\"status\":\"PAID\"}";

        HttpResponse<String> response = sendPut(
            "/api/orders/" + UUID.randomUUID() + "/status", body);
        assertNotEquals(400, response.statusCode());
    }

    private HttpResponse<String> sendPost(String path, String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + port + path))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> sendPut(String path, String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + port + path))
            .header("Content-Type", "application/json")
            .PUT(HttpRequest.BodyPublishers.ofString(body))
            .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
