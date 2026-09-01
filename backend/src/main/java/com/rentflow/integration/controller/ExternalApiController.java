package com.rentflow.integration.controller;

import com.rentflow.integration.dto.*;
import com.rentflow.integration.security.ExternalApiScope;
import com.rentflow.integration.service.ExternalApiService;
import com.rentflow.security.SecurityUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/external")
public class ExternalApiController {

    private final ExternalApiService externalApiService;

    public ExternalApiController(ExternalApiService externalApiService) {
        this.externalApiService = externalApiService;
    }

    @GetMapping("/customers")
    @ExternalApiScope("customers:read")
    public List<ExternalCustomerDTO> getCustomers() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return externalApiService.getCustomers(tenantId);
    }

    @GetMapping("/customers/{id}")
    @ExternalApiScope("customers:read")
    public ResponseEntity<ExternalCustomerDTO> getCustomer(@PathVariable UUID id) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return externalApiService.getCustomer(tenantId, id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/customers")
    @ExternalApiScope("customers:write")
    public ResponseEntity<ExternalCustomerDTO> createCustomer(@RequestBody ExternalCustomerDTO dto) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        ExternalCustomerDTO created = externalApiService.createCustomer(tenantId, dto);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/products")
    @ExternalApiScope("products:read")
    public List<ExternalProductDTO> getProducts() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return externalApiService.getProducts(tenantId);
    }

    @GetMapping("/products/{id}")
    @ExternalApiScope("products:read")
    public ResponseEntity<ExternalProductDTO> getProduct(@PathVariable UUID id) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return externalApiService.getProduct(tenantId, id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/inventory/availability")
    @ExternalApiScope("inventory:read")
    public ResponseEntity<ExternalAvailabilityDTO> checkAvailability(
            @RequestParam UUID productId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        ExternalAvailabilityDTO availability = externalApiService.checkAvailability(tenantId, productId, startDate, endDate);
        return ResponseEntity.ok(availability);
    }

    @GetMapping("/bookings")
    @ExternalApiScope("bookings:read")
    public List<ExternalBookingDTO> getBookings() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return externalApiService.getBookings(tenantId);
    }

    @GetMapping("/bookings/{id}")
    @ExternalApiScope("bookings:read")
    public ResponseEntity<ExternalBookingDTO> getBooking(@PathVariable UUID id) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return externalApiService.getBooking(tenantId, id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/invoices")
    @ExternalApiScope("invoices:read")
    public List<ExternalInvoiceDTO> getInvoices() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return externalApiService.getInvoices(tenantId);
    }

    @PostMapping("/quote-requests")
    @ExternalApiScope("quotes:write")
    public ResponseEntity<Map<String, Object>> createQuoteRequest(@RequestBody ExternalQuoteRequestDTO dto) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        Map<String, Object> result = externalApiService.createQuoteRequest(tenantId, dto);
        return ResponseEntity.ok(result);
    }
}
