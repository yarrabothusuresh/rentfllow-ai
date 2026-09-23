package com.rentflow.ai.controller;

import com.rentflow.ai.dto.BookingDTO;
import com.rentflow.ai.dto.BookingUnavailableDTO;
import com.rentflow.ai.exception.BookingUnavailableException;
import com.rentflow.ai.mock.DemoDataRepository;
import com.rentflow.ai.service.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final com.rentflow.security.CurrentUserService currentUserService;

    public BookingController(BookingService bookingService, com.rentflow.security.CurrentUserService currentUserService) {
        this.bookingService = bookingService;
        this.currentUserService = currentUserService;
    }

    private String resolveTenantId(String ignoredHeader) {
        return currentUserService.requireTenantId();
    }

    private String resolveRole(String ignoredHeader) {
        return currentUserService.requireRole();
    }

    @PostMapping("/from-quote/{quoteId}")
    public ResponseEntity<?> createBookingFromQuote(
            @PathVariable("quoteId") UUID quoteId,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {

        try {
            String tenantId = resolveTenantId(tenantHeader);
            String role = resolveRole(roleHeader);

            BookingDTO created = bookingService.createBookingFromQuote(tenantId, quoteId, role);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (BookingUnavailableException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getErrorDetails());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage() != null ? e.getMessage() : e.toString()));
        }
    }

    @PostMapping("/{bookingId}/confirm")
    public ResponseEntity<?> confirmBooking(
            @PathVariable("bookingId") UUID bookingId,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {

        try {
            String role = resolveRole(roleHeader);
            if ("DRIVER".equals(role) || "CUSTOMER".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You do not have permission to confirm this booking."));
            }

            String tenantId = resolveTenantId(tenantHeader);
            BookingDTO confirmed = bookingService.confirmBooking(tenantId, bookingId, role);
            return ResponseEntity.ok(confirmed);
        } catch (BookingUnavailableException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getErrorDetails());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage() != null ? e.getMessage() : e.toString()));
        }
    }

    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<?> cancelBooking(
            @PathVariable("bookingId") UUID bookingId,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {

        try {
            String role = resolveRole(roleHeader);
            if ("DRIVER".equals(role) || "WAREHOUSE".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You do not have permission to cancel bookings."));
            }

            String tenantId = resolveTenantId(tenantHeader);
            BookingDTO cancelled = bookingService.cancelBooking(tenantId, bookingId, role);
            return ResponseEntity.ok(cancelled);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage() != null ? e.getMessage() : e.toString()));
        }
    }

    private static final java.util.Set<String> BOOKING_SORT_FIELDS = java.util.Set.of(
            "createdAt", "bookingNumber", "bookingDate", "rentalStartDateTime", "rentalEndDateTime", "status", "totalAmount"
    );

    @GetMapping
    public ResponseEntity<?> getBookings(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "status", required = false) com.rentflow.ai.model.BookingStatus status,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "direction", required = false) String direction,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {

        String tenantId = resolveTenantId(tenantHeader);
        String role = resolveRole(roleHeader);

        int effectivePage = (page != null) ? page : com.rentflow.common.pagination.PaginationUtil.DEFAULT_PAGE;
        int effectiveSize = (size != null) ? size : com.rentflow.common.pagination.PaginationUtil.DEFAULT_PAGE_SIZE;

        org.springframework.data.domain.Pageable pageable = com.rentflow.common.pagination.PaginationUtil.createPageRequest(
                effectivePage, effectiveSize, sortBy, direction, BOOKING_SORT_FIELDS, "createdAt"
        );

        return ResponseEntity.ok(bookingService.getBookings(tenantId, status, pageable, role));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBookingById(
            @PathVariable("id") UUID id,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {

        try {
            String tenantId = resolveTenantId(tenantHeader);
            String role = resolveRole(roleHeader);
            return bookingService.getBookingById(tenantId, id, role)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage() != null ? e.getMessage() : e.toString()));
        }
    }
}
