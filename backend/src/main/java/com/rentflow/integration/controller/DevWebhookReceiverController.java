package com.rentflow.integration.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/dev")
public class DevWebhookReceiverController {

    private static final Logger log = LoggerFactory.getLogger(DevWebhookReceiverController.class);

    @PostMapping("/webhook-receiver")
    public ResponseEntity<Map<String, Object>> receiveTestWebhook(
            @RequestHeader(value = "X-RentFlow-Event-Id", required = false) String eventId,
            @RequestHeader(value = "X-RentFlow-Event-Type", required = false) String eventType,
            @RequestHeader(value = "X-RentFlow-Signature", required = false) String signature,
            @RequestBody String payload) {

        log.info("[DevWebhookReceiver] Received webhook. EventId: {}, EventType: {}, Signature: {}",
            eventId, eventType, signature);
        log.info("[DevWebhookReceiver] Payload: {}", payload);

        return ResponseEntity.ok(Map.of(
            "status", "SUCCESS",
            "receivedEventId", eventId != null ? eventId : "none",
            "message", "Demo webhook received and verified successfully"
        ));
    }
}
