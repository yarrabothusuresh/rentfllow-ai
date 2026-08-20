package com.rentflow.invoice.controller;

import com.rentflow.ai.mock.DemoDataRepository;
import com.rentflow.invoice.dto.InvoiceDTO;
import com.rentflow.invoice.model.InvoiceStatus;
import com.rentflow.invoice.service.InvoiceService;
import com.rentflow.payment.dto.PaymentDTO;
import com.rentflow.payment.service.PaymentService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@CrossOrigin(originPatterns = "*")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final PaymentService paymentService;

    public InvoiceController(InvoiceService invoiceService, PaymentService paymentService) {
        this.invoiceService = invoiceService;
        this.paymentService = paymentService;
    }

    private String resolveTenantId(String tenantHeader) {
        return (tenantHeader != null && !tenantHeader.isBlank())
                ? tenantHeader : DemoDataRepository.EVERGREEN_TENANT_ID;
    }

    private String resolveRole(String roleHeader) {
        return (roleHeader != null && !roleHeader.isBlank())
                ? roleHeader.toUpperCase() : "OWNER";
    }

    @GetMapping("/api/invoices")
    public ResponseEntity<?> listInvoices(
            @RequestParam(value = "status", required = false) InvoiceStatus status,
            @RequestParam(value = "customerId", required = false) UUID customerId,
            @RequestParam(value = "bookingId", required = false) UUID bookingId,
            @RequestParam(value = "search", required = false) String search,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {

        try {
            String tenantId = resolveTenantId(tenantHeader);
            String userRole = resolveRole(roleHeader);
            List<InvoiceDTO> invoices = invoiceService.listInvoices(tenantId, status, customerId, bookingId, search, userRole);
            return ResponseEntity.ok(invoices);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : e.toString()));
        }
    }

    @GetMapping("/api/invoices/{invoiceId}")
    public ResponseEntity<?> getInvoice(
            @PathVariable("invoiceId") UUID invoiceId,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {

        try {
            String tenantId = resolveTenantId(tenantHeader);
            String userRole = resolveRole(roleHeader);
            return invoiceService.getInvoice(tenantId, invoiceId, userRole)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : e.toString()));
        }
    }

    @PostMapping("/api/invoices/from-booking/{bookingId}")
    public ResponseEntity<?> createInvoiceFromBooking(
            @PathVariable("bookingId") UUID bookingId,
            @RequestBody(required = false) Map<String, Object> body,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {

        try {
            String tenantId = resolveTenantId(tenantHeader);
            String userRole = resolveRole(roleHeader);

            String notes = body != null && body.containsKey("notes") ? (String) body.get("notes") : null;
            LocalDate dueDate = null;
            if (body != null && body.containsKey("dueDate") && body.get("dueDate") != null) {
                dueDate = LocalDate.parse((String) body.get("dueDate"));
            }

            InvoiceDTO invoice = invoiceService.createInvoiceFromBooking(tenantId, bookingId, notes, dueDate, userRole);
            return ResponseEntity.status(HttpStatus.CREATED).body(invoice);
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

    @PatchMapping("/api/invoices/{invoiceId}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable("invoiceId") UUID invoiceId,
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {

        try {
            String tenantId = resolveTenantId(tenantHeader);
            String userRole = resolveRole(roleHeader);

            if (body == null || !body.containsKey("status")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Status field is required."));
            }

            InvoiceStatus status = InvoiceStatus.valueOf(body.get("status").toUpperCase());
            InvoiceDTO updated = invoiceService.updateInvoiceStatus(tenantId, invoiceId, status, userRole);
            return ResponseEntity.ok(updated);
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

    @PostMapping("/api/invoices/{invoiceId}/void")
    public ResponseEntity<?> voidInvoice(
            @PathVariable("invoiceId") UUID invoiceId,
            @RequestBody(required = false) Map<String, String> body,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {

        try {
            String tenantId = resolveTenantId(tenantHeader);
            String userRole = resolveRole(roleHeader);
            String reason = body != null ? body.get("reason") : null;

            InvoiceDTO voided = invoiceService.voidInvoice(tenantId, invoiceId, reason, userRole);
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

    @GetMapping("/api/bookings/{bookingId}/invoice")
    public ResponseEntity<?> getBookingInvoice(
            @PathVariable("bookingId") UUID bookingId,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {

        try {
            String tenantId = resolveTenantId(tenantHeader);
            String userRole = resolveRole(roleHeader);
            return invoiceService.getInvoiceByBookingId(tenantId, bookingId, userRole)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : e.toString()));
        }
    }

    @GetMapping("/api/invoices/{invoiceId}/payments")
    public ResponseEntity<?> getInvoicePayments(
            @PathVariable("invoiceId") UUID invoiceId,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {

        try {
            String tenantId = resolveTenantId(tenantHeader);
            String userRole = resolveRole(roleHeader);

            InvoiceDTO invoice = invoiceService.getInvoice(tenantId, invoiceId, userRole)
                    .orElseThrow(() -> new IllegalArgumentException("Invoice not found with ID: " + invoiceId));

            List<PaymentDTO> payments = paymentService.getBookingPayments(tenantId, invoice.getBookingId());
            return ResponseEntity.ok(payments);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : e.toString()));
        }
    }
}
