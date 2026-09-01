package com.rentflow.integration.service;

import com.rentflow.integration.model.*;
import com.rentflow.integration.repository.WebhookDeliveryRepository;
import com.rentflow.integration.repository.WebhookEndpointRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class WebhookDeliveryService {

    private static final Logger log = LoggerFactory.getLogger(WebhookDeliveryService.class);
    private static final int MAX_ATTEMPTS = 5;

    private final WebhookDeliveryRepository deliveryRepository;
    private final WebhookEndpointRepository endpointRepository;
    private final IntegrationCredentialService credentialService;

    public WebhookDeliveryService(WebhookDeliveryRepository deliveryRepository,
                                  WebhookEndpointRepository endpointRepository,
                                  IntegrationCredentialService credentialService) {
        this.deliveryRepository = deliveryRepository;
        this.endpointRepository = endpointRepository;
        this.credentialService = credentialService;
    }

    public WebhookDelivery dispatchWebhook(WebhookEndpoint endpoint, String eventId, String eventType, String payload, int attemptNumber) {
        WebhookDelivery delivery = new WebhookDelivery();
        delivery.setTenantId(endpoint.getTenantId());
        delivery.setWebhookEndpointId(endpoint.getId());
        delivery.setIntegrationEventId(eventId);
        delivery.setEventType(eventType);
        delivery.setAttemptNumber(attemptNumber);
        delivery.setRequestPayload(payload);
        delivery.setStartedAt(LocalDateTime.now());
        delivery.setStatus(DeliveryStatus.PENDING);

        String secret = credentialService.decryptCredential(endpoint.getSecretReference());
        if (secret == null || secret.isBlank()) {
            secret = "rf_sec_default";
        }

        String timestamp = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
        String signature = generateHmacSha256(secret, timestamp + "." + payload);

        String headersSummary = "X-RentFlow-Event-Id: " + eventId + "\n" +
                                "X-RentFlow-Event-Type: " + eventType + "\n" +
                                "X-RentFlow-Timestamp: " + timestamp + "\n" +
                                "X-RentFlow-Signature: " + signature;
        delivery.setRequestHeaders(headersSummary);

        long start = System.currentTimeMillis();
        try {
            // Send outbound HTTP request
            URL url = new URL(endpoint.getEndpointUrl());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("User-Agent", "RentFlow-Webhook-Dispatcher/1.0");
            conn.setRequestProperty("X-RentFlow-Event-Id", eventId);
            conn.setRequestProperty("X-RentFlow-Event-Type", eventType);
            conn.setRequestProperty("X-RentFlow-Timestamp", timestamp);
            conn.setRequestProperty("X-RentFlow-Signature", signature);
            conn.setConnectTimeout(4000);
            conn.setReadTimeout(4000);
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = payload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            delivery.setHttpStatus(responseCode);
            long duration = System.currentTimeMillis() - start;
            delivery.setDurationMs(duration);
            delivery.setCompletedAt(LocalDateTime.now());

            String responseBody = readResponseStream(conn, responseCode);
            delivery.setResponseSummary(responseBody);

            if (responseCode >= 200 && responseCode < 300) {
                delivery.setStatus(DeliveryStatus.SUCCESS);
                log.info("[WebhookDelivery] Webhook {} delivered successfully to {} (HTTP {}) in {}ms",
                    delivery.getId(), endpoint.getEndpointUrl(), responseCode, duration);
            } else if (isRetryable(responseCode)) {
                handleRetryOrDeadLetter(delivery, attemptNumber, "HTTP " + responseCode + ": " + responseBody);
            } else {
                // Non-retryable 4xx client error
                delivery.setStatus(DeliveryStatus.FAILED);
                log.warn("[WebhookDelivery] Non-retryable HTTP {} from {}: {}", responseCode, endpoint.getEndpointUrl(), responseBody);
            }

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - start;
            delivery.setDurationMs(duration);
            delivery.setCompletedAt(LocalDateTime.now());
            delivery.setHttpStatus(0);
            delivery.setResponseSummary("Connection Error: " + e.getMessage());
            handleRetryOrDeadLetter(delivery, attemptNumber, e.getMessage());
        }

        return deliveryRepository.save(delivery);
    }

    private void handleRetryOrDeadLetter(WebhookDelivery delivery, int attemptNumber, String errorMsg) {
        if (attemptNumber >= MAX_ATTEMPTS) {
            delivery.setStatus(DeliveryStatus.DEAD_LETTER);
            delivery.setNextRetryAt(null);
            log.error("[WebhookDelivery] Delivery exceeded max attempts ({}), marked DEAD_LETTER: {}", MAX_ATTEMPTS, errorMsg);
        } else {
            delivery.setStatus(DeliveryStatus.RETRYING);
            long backoffSeconds = calculateBackoffSeconds(attemptNumber);
            delivery.setNextRetryAt(LocalDateTime.now().plusSeconds(backoffSeconds));
            log.warn("[WebhookDelivery] Attempt {} failed. Next retry in {}s. Error: {}", attemptNumber, backoffSeconds, errorMsg);
        }
    }

    public long calculateBackoffSeconds(int attemptNumber) {
        return switch (attemptNumber) {
            case 1 -> 60;        // 1 min
            case 2 -> 300;       // 5 min
            case 3 -> 1800;      // 30 min
            case 4 -> 7200;      // 2 hours
            default -> 14400;    // 4 hours
        };
    }

    public boolean isRetryable(int statusCode) {
        return statusCode == 408 || statusCode == 429 || statusCode >= 500;
    }

    public String generateHmacSha256(String secret, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(rawHmac);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to generate HMAC SHA256 signature", e);
        }
    }

    private String readResponseStream(HttpURLConnection conn, int responseCode) {
        try {
            var stream = (responseCode >= 200 && responseCode < 400) ? conn.getInputStream() : conn.getErrorStream();
            if (stream == null) return "No response body";
            try (BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line.trim());
                    if (response.length() > 1000) break; // truncate long response
                }
                return response.toString();
            }
        } catch (Exception e) {
            return "Unable to read response stream: " + e.getMessage();
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
