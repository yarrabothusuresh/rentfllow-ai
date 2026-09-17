package com.rentflow.portal.controller;

import com.rentflow.ai.dto.QuoteDTO;
import com.rentflow.portal.dto.*;
import com.rentflow.portal.service.CartService;
import com.rentflow.portal.service.PublicQuoteRequestService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/public")
public class PublicCartController {

    private final CartService cartService;
    private final PublicQuoteRequestService quoteRequestService;

    public PublicCartController(CartService cartService, PublicQuoteRequestService quoteRequestService) {
        this.cartService = cartService;
        this.quoteRequestService = quoteRequestService;
    }

    private String resolveTenantId(String tenantHeader) {
        return (tenantHeader != null && !tenantHeader.isBlank()) ? tenantHeader : "99999999-9999-9999-9999-999999999999";
    }

    private UUID resolveCustomerId(String customerHeader) {
        if (customerHeader != null && !customerHeader.isBlank()) {
            try { return UUID.fromString(customerHeader); } catch (Exception e) {}
        }
        return null;
    }

    @GetMapping("/cart")
    public ResponseEntity<CartDTO> getCart(
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-Cart-Token", required = false) String cartToken,
            @RequestHeader(value = "X-Customer-Id", required = false) String customerHeader) {
        String tenantId = resolveTenantId(tenantHeader);
        UUID customerId = resolveCustomerId(customerHeader);
        return ResponseEntity.ok(cartService.getCart(tenantId, cartToken, customerId));
    }

    @PostMapping("/cart/items")
    public ResponseEntity<CartDTO> addItemToCart(
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-Cart-Token", required = false) String cartToken,
            @RequestHeader(value = "X-Customer-Id", required = false) String customerHeader,
            @RequestBody Map<String, Object> body) {
        String tenantId = resolveTenantId(tenantHeader);
        UUID customerId = resolveCustomerId(customerHeader);

        UUID productId = UUID.fromString((String) body.get("productId"));
        int quantity = body.get("quantity") != null ? ((Number) body.get("quantity")).intValue() : 1;

        LocalDateTime start = body.get("startDateTime") != null ? LocalDateTime.parse((String) body.get("startDateTime")) : null;
        LocalDateTime end = body.get("endDateTime") != null ? LocalDateTime.parse((String) body.get("endDateTime")) : null;

        return ResponseEntity.ok(cartService.addItemToCart(tenantId, cartToken, customerId, productId, quantity, start, end));
    }

    @PatchMapping("/cart/items/{id}")
    public ResponseEntity<CartDTO> updateCartItem(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-Cart-Token", required = false) String cartToken,
            @RequestHeader(value = "X-Customer-Id", required = false) String customerHeader,
            @RequestBody Map<String, Object> body) {
        String tenantId = resolveTenantId(tenantHeader);
        UUID customerId = resolveCustomerId(customerHeader);

        int quantity = body.get("quantity") != null ? ((Number) body.get("quantity")).intValue() : 1;
        LocalDateTime start = body.get("startDateTime") != null ? LocalDateTime.parse((String) body.get("startDateTime")) : null;
        LocalDateTime end = body.get("endDateTime") != null ? LocalDateTime.parse((String) body.get("endDateTime")) : null;

        return ResponseEntity.ok(cartService.updateCartItem(tenantId, cartToken, customerId, id, quantity, start, end));
    }

    @DeleteMapping("/cart/items/{id}")
    public ResponseEntity<CartDTO> removeItemFromCart(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-Cart-Token", required = false) String cartToken,
            @RequestHeader(value = "X-Customer-Id", required = false) String customerHeader) {
        String tenantId = resolveTenantId(tenantHeader);
        UUID customerId = resolveCustomerId(customerHeader);
        return ResponseEntity.ok(cartService.removeItemFromCart(tenantId, cartToken, customerId, id));
    }

    @PostMapping("/cart/validate")
    public ResponseEntity<CartDTO> validateCart(
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-Cart-Token", required = false) String cartToken,
            @RequestHeader(value = "X-Customer-Id", required = false) String customerHeader) {
        String tenantId = resolveTenantId(tenantHeader);
        UUID customerId = resolveCustomerId(customerHeader);
        return ResponseEntity.ok(cartService.validateCart(tenantId, cartToken, customerId));
    }

    @PostMapping("/quote-requests")
    public ResponseEntity<QuoteDTO> submitQuoteRequest(
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-Customer-Id", required = false) String customerHeader,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKeyHeader,
            @RequestBody PublicQuoteRequestDTO dto) {
        String tenantId = resolveTenantId(tenantHeader);
        UUID customerId = resolveCustomerId(customerHeader);
        if (idempotencyKeyHeader != null && !idempotencyKeyHeader.isBlank()) {
            dto.setIdempotencyKey(idempotencyKeyHeader.trim());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(quoteRequestService.submitPublicQuoteRequest(tenantId, customerId, dto));
    }
}
