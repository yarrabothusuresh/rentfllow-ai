package com.rentflow.portal.controller;

import com.rentflow.claims.dto.DamageClaimDTO;
import com.rentflow.payment.dto.PaymentDTO;
import com.rentflow.portal.dto.*;
import com.rentflow.portal.service.CustomerAddressService;
import com.rentflow.portal.service.CustomerMessagingService;
import com.rentflow.portal.service.CustomerPortalService;
import com.rentflow.security.CurrentUserService;
import com.rentflow.common.pagination.PaginationUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Customer Portal API Controller.
 * Derives customer and tenant identity strictly from verified server-side JWT authentication.
 * Spoofing via X-Customer-Id or X-Tenant-Id headers is strictly blocked.
 */
@RestController
@RequestMapping("/api/portal")
public class CustomerPortalController {

    private final CustomerPortalService portalService;
    private final CustomerAddressService addressService;
    private final CustomerMessagingService messagingService;
    private final CurrentUserService currentUserService;

    private static final Set<String> PORTAL_QUOTE_SORT_FIELDS = Set.of(
            "id", "quoteNumber", "status", "quoteDate", "validUntil", "rentalStartDateTime",
            "rentalEndDateTime", "totalAmount", "createdAt", "updatedAt"
    );

    private static final Set<String> PORTAL_BOOKING_SORT_FIELDS = Set.of(
            "id", "bookingNumber", "status", "bookingDate", "rentalStartDateTime",
            "rentalEndDateTime", "totalAmount", "createdAt", "updatedAt"
    );

    private static final Set<String> PORTAL_INVOICE_SORT_FIELDS = Set.of(
            "id", "invoiceNumber", "status", "issueDate", "dueDate", "totalAmount",
            "amountPaid", "balanceDue", "createdAt", "updatedAt"
    );

    public CustomerPortalController(CustomerPortalService portalService,
                                    CustomerAddressService addressService,
                                    CustomerMessagingService messagingService,
                                    CurrentUserService currentUserService) {
        this.portalService = portalService;
        this.addressService = addressService;
        this.messagingService = messagingService;
        this.currentUserService = currentUserService;
    }

    private String resolveTenantId() {
        return currentUserService.requireTenantId();
    }

    private UUID resolveCustomerId() {
        return currentUserService.requireCustomerId();
    }

    @PostMapping("/auth/register")
    public ResponseEntity<?> register(
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestBody CustomerRegistrationRequestDTO request) {
        try {
            String tenantId = (tenantHeader != null && !tenantHeader.isBlank())
                    ? tenantHeader.trim()
                    : "99999999-9999-9999-9999-999999999999";
            CustomerAuthResponseDTO response = portalService.register(tenantId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
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
    public ResponseEntity<CustomerPortalDashboardDTO> getDashboard() {
        return ResponseEntity.ok(portalService.getDashboard(resolveTenantId(), resolveCustomerId()));
    }

    @GetMapping("/profile")
    public ResponseEntity<CustomerProfileDTO> getProfile() {
        return ResponseEntity.ok(portalService.getProfile(resolveTenantId(), resolveCustomerId()));
    }

    @PutMapping("/profile")
    public ResponseEntity<CustomerProfileDTO> updateProfile(@RequestBody CustomerProfileDTO dto) {
        return ResponseEntity.ok(portalService.updateProfile(resolveTenantId(), resolveCustomerId(), dto));
    }

    @GetMapping("/events")
    public ResponseEntity<List<CustomerPortalEventDTO>> getEvents() {
        return ResponseEntity.ok(portalService.getCustomerEvents(resolveTenantId(), resolveCustomerId()));
    }

    @GetMapping("/events/{id}")
    public ResponseEntity<?> getEventDetail(@PathVariable UUID id) {
        return ResponseEntity.ok(portalService.getEventDetail(resolveTenantId(), resolveCustomerId(), id));
    }

    @GetMapping("/quotes")
    public ResponseEntity<Page<CustomerPortalQuoteDTO>> getQuotes(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(name = "direction", defaultValue = "desc") String direction) {
        Pageable pageable = PaginationUtil.createPageRequest(page, size, sortBy, direction, PORTAL_QUOTE_SORT_FIELDS);
        return ResponseEntity.ok(portalService.getCustomerQuotes(resolveTenantId(), resolveCustomerId(), pageable));
    }

    @GetMapping("/quotes/{id}")
    public ResponseEntity<?> getQuoteDetail(@PathVariable UUID id) {
        return ResponseEntity.ok(portalService.getQuoteDetail(resolveTenantId(), resolveCustomerId(), id));
    }

    @PostMapping("/quotes/{id}/approve")
    public ResponseEntity<?> approveQuote(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(portalService.acceptQuote(resolveTenantId(), resolveCustomerId(), id, "CUSTOMER"));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/quotes/{id}/accept")
    public ResponseEntity<?> acceptQuote(@PathVariable UUID id) {
        return approveQuote(id);
    }

    @PostMapping("/quotes/{id}/decline")
    public ResponseEntity<?> declineQuote(
            @PathVariable UUID id,
            @RequestBody(required = false) QuoteDeclineRequestDTO req) {
        String reason = req != null ? req.getReason() : "Customer declined quote.";
        return ResponseEntity.ok(portalService.declineQuote(resolveTenantId(), resolveCustomerId(), id, reason));
    }

    @PostMapping("/quotes/{id}/request-changes")
    public ResponseEntity<?> requestQuoteChanges(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        String message = body != null ? body.get("message") : "Customer requested changes.";
        return ResponseEntity.ok(portalService.requestQuoteChanges(resolveTenantId(), resolveCustomerId(), id, message, "CUSTOMER"));
    }

    @GetMapping("/bookings")
    public ResponseEntity<Page<CustomerPortalBookingDTO>> getBookings(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(name = "direction", defaultValue = "desc") String direction) {
        Pageable pageable = PaginationUtil.createPageRequest(page, size, sortBy, direction, PORTAL_BOOKING_SORT_FIELDS);
        return ResponseEntity.ok(portalService.getCustomerBookings(resolveTenantId(), resolveCustomerId(), pageable));
    }

    @GetMapping("/bookings/{id}")
    public ResponseEntity<?> getBookingDetail(@PathVariable UUID id) {
        return ResponseEntity.ok(portalService.getBookingDetail(resolveTenantId(), resolveCustomerId(), id));
    }

    @GetMapping("/invoices")
    public ResponseEntity<Page<CustomerPortalInvoiceDTO>> getInvoices(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(name = "direction", defaultValue = "desc") String direction) {
        Pageable pageable = PaginationUtil.createPageRequest(page, size, sortBy, direction, PORTAL_INVOICE_SORT_FIELDS);
        return ResponseEntity.ok(portalService.getCustomerInvoices(resolveTenantId(), resolveCustomerId(), pageable));
    }

    @GetMapping("/invoices/{id}")
    public ResponseEntity<?> getInvoiceDetail(@PathVariable UUID id) {
        return ResponseEntity.ok(portalService.getInvoiceDetail(resolveTenantId(), resolveCustomerId(), id));
    }

    @GetMapping("/invoices/{id}/payments")
    public ResponseEntity<List<PaymentDTO>> getInvoicePayments(@PathVariable UUID id) {
        return ResponseEntity.ok(portalService.getInvoicePayments(resolveTenantId(), resolveCustomerId(), id));
    }

    @PostMapping("/invoices/{id}/pay")
    public ResponseEntity<?> payInvoice(
            @PathVariable UUID id,
            @RequestBody com.rentflow.payment.dto.RecordPaymentDTO dto) {
        try {
            PaymentDTO payment = portalService.payInvoice(resolveTenantId(), resolveCustomerId(), id, dto);
            if (Boolean.TRUE.equals(payment.getIdempotentReplay())) {
                return ResponseEntity.ok(payment);
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(payment);
        } catch (com.rentflow.payment.exception.IdempotencyConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/payments")
    public ResponseEntity<?> getPayments() {
        String tenantId = resolveTenantId();
        UUID customerId = resolveCustomerId();
        List<CustomerPortalInvoiceDTO> invoices = portalService.getCustomerInvoices(tenantId, customerId);
        List<PaymentDTO> payments = new java.util.ArrayList<>();
        for (CustomerPortalInvoiceDTO inv : invoices) {
            if (inv.getId() != null) {
                payments.addAll(portalService.getInvoicePayments(tenantId, customerId, inv.getId()));
            }
        }
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/claims")
    public ResponseEntity<List<DamageClaimDTO>> getClaims() {
        return ResponseEntity.ok(portalService.getCustomerClaims(resolveTenantId(), resolveCustomerId()));
    }

    @GetMapping("/claims/{id}")
    public ResponseEntity<?> getClaimDetail(@PathVariable UUID id) {
        return ResponseEntity.ok(portalService.getCustomerClaimDetail(resolveTenantId(), resolveCustomerId(), id));
    }

    @PostMapping("/claims/{id}/approve")
    public ResponseEntity<?> approveClaim(@PathVariable UUID id) {
        return ResponseEntity.ok(portalService.approveClaim(resolveTenantId(), resolveCustomerId(), id));
    }

    @PostMapping("/claims/{id}/dispute")
    public ResponseEntity<?> disputeClaim(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        String reason = body != null ? body.get("reason") : "Customer disputed claim";
        return ResponseEntity.ok(portalService.disputeClaim(resolveTenantId(), resolveCustomerId(), id, reason));
    }

    @GetMapping("/messages")
    public ResponseEntity<List<CustomerConversationDTO>> getConversations() {
        return ResponseEntity.ok(messagingService.getCustomerConversations(resolveTenantId(), resolveCustomerId()));
    }

    @PostMapping("/messages")
    public ResponseEntity<CustomerConversationDTO> sendMessage(@RequestBody CreateMessageRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                messagingService.createOrReplyMessage(resolveTenantId(), resolveCustomerId(), dto, "CUSTOMER")
        );
    }

    @GetMapping("/addresses")
    public ResponseEntity<List<CustomerAddressDTO>> getAddresses() {
        return ResponseEntity.ok(addressService.getCustomerAddresses(resolveTenantId(), resolveCustomerId()));
    }

    @PostMapping("/addresses")
    public ResponseEntity<CustomerAddressDTO> createAddress(@RequestBody CustomerAddressDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                addressService.createAddress(resolveTenantId(), resolveCustomerId(), dto)
        );
    }

    @PatchMapping("/addresses/{id}")
    public ResponseEntity<?> updateAddress(
            @PathVariable UUID id,
            @RequestBody CustomerAddressDTO dto) {
        return ResponseEntity.ok(addressService.updateAddress(resolveTenantId(), resolveCustomerId(), id, dto));
    }

    @DeleteMapping("/addresses/{id}")
    public ResponseEntity<?> deleteAddress(@PathVariable UUID id) {
        addressService.deleteAddress(resolveTenantId(), resolveCustomerId(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/requests")
    public ResponseEntity<List<CustomerRequestDTO>> getRequests() {
        return ResponseEntity.ok(portalService.getCustomerRequests(resolveTenantId(), resolveCustomerId()));
    }

    @PostMapping("/requests")
    public ResponseEntity<CustomerRequestDTO> createRequest(@RequestBody CreateCustomerRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                portalService.createCustomerRequest(resolveTenantId(), resolveCustomerId(), dto)
        );
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<?> handleSecurityException(SecurityException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
    }
}
