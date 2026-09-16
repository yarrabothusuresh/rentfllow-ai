package com.rentflow.payment.controller;

import com.rentflow.payment.dto.BookingFinancialSummaryDTO;
import com.rentflow.payment.dto.PaymentDTO;
import com.rentflow.payment.dto.RecordPaymentDTO;
import com.rentflow.payment.exception.IdempotencyConflictException;
import com.rentflow.payment.service.PaymentService;
import com.rentflow.security.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class PaymentController {

    private final PaymentService paymentService;
    private final CurrentUserService currentUserService;

    public PaymentController(PaymentService paymentService, CurrentUserService currentUserService) {
        this.paymentService = paymentService;
        this.currentUserService = currentUserService;
    }

    private String resolveTenantId(String ignoredHeader) {
        return currentUserService.requireTenantId();
    }

    private String resolveRole(String ignoredHeader) {
        return currentUserService.requireRole();
    }

    @GetMapping("/api/bookings/{bookingId}/payments")
    public ResponseEntity<?> getBookingPayments(
            @PathVariable("bookingId") UUID bookingId,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {

        try {
            String tenantId = resolveTenantId(tenantHeader);
            List<PaymentDTO> payments = paymentService.getBookingPayments(tenantId, bookingId);
            return ResponseEntity.ok(payments);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : e.toString()));
        }
    }

    @PostMapping("/api/bookings/{bookingId}/payments")
    public ResponseEntity<?> recordBookingPayment(
            @PathVariable("bookingId") UUID bookingId,
            @Valid @RequestBody RecordPaymentDTO dto,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {

        return executeRecordPayment(bookingId, dto, tenantHeader, roleHeader);
    }

    @PostMapping("/api/invoices/{invoiceId}/payments")
    public ResponseEntity<?> recordInvoicePayment(
            @PathVariable("invoiceId") UUID invoiceId,
            @Valid @RequestBody RecordPaymentDTO dto,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {

        dto.setInvoiceId(invoiceId);
        return executeRecordPayment(null, dto, tenantHeader, roleHeader);
    }

    @PostMapping("/api/payments")
    public ResponseEntity<?> recordPayment(
            @Valid @RequestBody RecordPaymentDTO dto,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {

        return executeRecordPayment(dto.getBookingId(), dto, tenantHeader, roleHeader);
    }

    private ResponseEntity<?> executeRecordPayment(UUID bookingId, RecordPaymentDTO dto, String tenantHeader, String roleHeader) {
        try {
            String tenantId = resolveTenantId(tenantHeader);
            String userRole = resolveRole(roleHeader);
            UUID customerId = currentUserService.getCustomerId().orElse(null);

            PaymentDTO created = paymentService.recordPayment(tenantId, bookingId, dto, userRole, customerId);

            if (Boolean.TRUE.equals(created.getIdempotentReplay())) {
                return ResponseEntity.ok(created);
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IdempotencyConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : e.toString()));
        }
    }

    @GetMapping("/api/payments/{paymentId}")
    public ResponseEntity<?> getPayment(
            @PathVariable("paymentId") UUID paymentId,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {

        try {
            String tenantId = resolveTenantId(tenantHeader);
            return paymentService.getPayment(tenantId, paymentId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : e.toString()));
        }
    }

    @PostMapping("/api/payments/{paymentId}/void")
    public ResponseEntity<?> voidPayment(
            @PathVariable("paymentId") UUID paymentId,
            @RequestBody(required = false) Map<String, String> body,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {

        try {
            String tenantId = resolveTenantId(tenantHeader);
            String userRole = resolveRole(roleHeader);
            String reason = (body != null) ? body.get("reason") : null;

            PaymentDTO voided = paymentService.voidPayment(tenantId, paymentId, userRole, reason);
            return ResponseEntity.ok(voided);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : e.toString()));
        }
    }

    @GetMapping("/api/bookings/{bookingId}/financial-summary")
    public ResponseEntity<?> getBookingFinancialSummary(
            @PathVariable("bookingId") UUID bookingId,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {

        try {
            String tenantId = resolveTenantId(tenantHeader);
            BookingFinancialSummaryDTO summary = paymentService.getFinancialSummary(tenantId, bookingId);
            return ResponseEntity.ok(summary);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : e.toString()));
        }
    }
}
