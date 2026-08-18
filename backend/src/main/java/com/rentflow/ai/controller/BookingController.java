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
@CrossOrigin(originPatterns = "*")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    private String resolveTenantId(String tenantHeader) {
        return (tenantHeader != null && !tenantHeader.isBlank())
                ? tenantHeader : DemoDataRepository.EVERGREEN_TENANT_ID;
    }

    private String resolveRole(String roleHeader) {
        return (roleHeader != null && !roleHeader.isBlank())
                ? roleHeader.toUpperCase() : "OWNER";
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

    @GetMapping
    public ResponseEntity<?> getBookings(
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {

        try {
            String tenantId = resolveTenantId(tenantHeader);
            String role = resolveRole(roleHeader);
            return ResponseEntity.ok(bookingService.getBookings(tenantId, role));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage() != null ? e.getMessage() : e.toString()));
        }
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
