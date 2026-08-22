package com.rentflow.portal.controller;

import com.rentflow.ai.service.CrmDataInitializer;
import com.rentflow.invoice.dto.InvoiceDTO;
import com.rentflow.payment.dto.PaymentDTO;
import com.rentflow.portal.dto.*;
import com.rentflow.portal.service.CustomerPortalService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/portal")
public class CustomerPortalController {

    private final CustomerPortalService portalService;

    public CustomerPortalController(CustomerPortalService portalService) {
        this.portalService = portalService;
    }

    private String resolveTenantId(String tenantHeader) {
        return (tenantHeader != null && !tenantHeader.isBlank()) ? tenantHeader : "99999999-9999-9999-9999-999999999999";
    }

    private UUID resolveCustomerId(String customerHeader) {
        if (customerHeader != null && !customerHeader.isBlank()) {
            try {
                return UUID.fromString(customerHeader);
            } catch (IllegalArgumentException e) {
                // fallback
            }
        }
        return CrmDataInitializer.EMILY_CUSTOMER_ID;
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody CustomerLoginRequestDTO request) {
        try {
            CustomerAuthResponseDTO response = portalService.login(request.getEmail(), request.getPassword());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/dashboard")
    public ResponseEntity<CustomerPortalDashboardDTO> getDashboard(
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-Customer-Id", required = false) String customerHeader) {
        String tenantId = resolveTenantId(tenantHeader);
        UUID customerId = resolveCustomerId(customerHeader);
        return ResponseEntity.ok(portalService.getDashboard(tenantId, customerId));
    }

    @GetMapping("/profile")
    public ResponseEntity<CustomerProfileDTO> getProfile(
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-Customer-Id", required = false) String customerHeader) {
        String tenantId = resolveTenantId(tenantHeader);
        UUID customerId = resolveCustomerId(customerHeader);
        return ResponseEntity.ok(portalService.getProfile(tenantId, customerId));
    }

    @PutMapping("/profile")
    public ResponseEntity<CustomerProfileDTO> updateProfile(
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-Customer-Id", required = false) String customerHeader,
            @RequestBody CustomerProfileDTO dto) {
        String tenantId = resolveTenantId(tenantHeader);
        UUID customerId = resolveCustomerId(customerHeader);
        return ResponseEntity.ok(portalService.updateProfile(tenantId, customerId, dto));
    }

    @GetMapping("/events")
    public ResponseEntity<List<CustomerPortalEventDTO>> getEvents(
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-Customer-Id", required = false) String customerHeader) {
        String tenantId = resolveTenantId(tenantHeader);
        UUID customerId = resolveCustomerId(customerHeader);
        return ResponseEntity.ok(portalService.getCustomerEvents(tenantId, customerId));
    }

    @GetMapping("/events/{id}")
    public ResponseEntity<?> getEventDetail(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-Customer-Id", required = false) String customerHeader) {
        try {
            String tenantId = resolveTenantId(tenantHeader);
            UUID customerId = resolveCustomerId(customerHeader);
            return ResponseEntity.ok(portalService.getEventDetail(tenantId, customerId, id));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/quotes")
    public ResponseEntity<List<CustomerPortalQuoteDTO>> getQuotes(
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-Customer-Id", required = false) String customerHeader) {
        String tenantId = resolveTenantId(tenantHeader);
        UUID customerId = resolveCustomerId(customerHeader);
        return ResponseEntity.ok(portalService.getCustomerQuotes(tenantId, customerId));
    }

    @GetMapping("/quotes/{id}")
    public ResponseEntity<?> getQuoteDetail(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-Customer-Id", required = false) String customerHeader) {
        try {
            String tenantId = resolveTenantId(tenantHeader);
            UUID customerId = resolveCustomerId(customerHeader);
            return ResponseEntity.ok(portalService.getQuoteDetail(tenantId, customerId, id));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/quotes/{id}/accept")
    public ResponseEntity<?> acceptQuote(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-Customer-Id", required = false) String customerHeader) {
        try {
            String tenantId = resolveTenantId(tenantHeader);
            UUID customerId = resolveCustomerId(customerHeader);
            return ResponseEntity.ok(portalService.acceptQuote(tenantId, customerId, id, "CUSTOMER"));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/quotes/{id}/request-changes")
    public ResponseEntity<?> requestQuoteChanges(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-Customer-Id", required = false) String customerHeader,
            @RequestBody Map<String, String> body) {
        try {
            String tenantId = resolveTenantId(tenantHeader);
            UUID customerId = resolveCustomerId(customerHeader);
            String message = body != null ? body.get("message") : "Customer requested changes.";
            return ResponseEntity.ok(portalService.requestQuoteChanges(tenantId, customerId, id, message, "CUSTOMER"));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/bookings")
    public ResponseEntity<List<CustomerPortalBookingDTO>> getBookings(
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-Customer-Id", required = false) String customerHeader) {
        String tenantId = resolveTenantId(tenantHeader);
        UUID customerId = resolveCustomerId(customerHeader);
        return ResponseEntity.ok(portalService.getCustomerBookings(tenantId, customerId));
    }

    @GetMapping("/bookings/{id}")
    public ResponseEntity<?> getBookingDetail(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-Customer-Id", required = false) String customerHeader) {
        try {
            String tenantId = resolveTenantId(tenantHeader);
            UUID customerId = resolveCustomerId(customerHeader);
            return ResponseEntity.ok(portalService.getBookingDetail(tenantId, customerId, id));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/invoices")
    public ResponseEntity<List<CustomerPortalInvoiceDTO>> getInvoices(
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-Customer-Id", required = false) String customerHeader) {
        String tenantId = resolveTenantId(tenantHeader);
        UUID customerId = resolveCustomerId(customerHeader);
        return ResponseEntity.ok(portalService.getCustomerInvoices(tenantId, customerId));
    }

    @GetMapping("/invoices/{id}")
    public ResponseEntity<?> getInvoiceDetail(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-Customer-Id", required = false) String customerHeader) {
        try {
            String tenantId = resolveTenantId(tenantHeader);
            UUID customerId = resolveCustomerId(customerHeader);
            return ResponseEntity.ok(portalService.getInvoiceDetail(tenantId, customerId, id));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/invoices/{id}/payments")
    public ResponseEntity<?> getInvoicePayments(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-Customer-Id", required = false) String customerHeader) {
        try {
            String tenantId = resolveTenantId(tenantHeader);
            UUID customerId = resolveCustomerId(customerHeader);
            return ResponseEntity.ok(portalService.getInvoicePayments(tenantId, customerId, id));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/requests")
    public ResponseEntity<List<CustomerRequestDTO>> getRequests(
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-Customer-Id", required = false) String customerHeader) {
        String tenantId = resolveTenantId(tenantHeader);
        UUID customerId = resolveCustomerId(customerHeader);
        return ResponseEntity.ok(portalService.getCustomerRequests(tenantId, customerId));
    }

    @PostMapping("/requests")
    public ResponseEntity<CustomerRequestDTO> createRequest(
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-Customer-Id", required = false) String customerHeader,
            @RequestBody CreateCustomerRequestDTO dto) {
        String tenantId = resolveTenantId(tenantHeader);
        UUID customerId = resolveCustomerId(customerHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body(portalService.createCustomerRequest(tenantId, customerId, dto));
    }
}
