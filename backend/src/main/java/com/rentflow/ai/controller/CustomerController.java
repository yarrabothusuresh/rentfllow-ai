package com.rentflow.ai.controller;

import com.rentflow.ai.dto.CustomerDTO;
import com.rentflow.ai.dto.EventDTO;
import com.rentflow.ai.mock.DemoDataRepository;
import com.rentflow.ai.service.CustomerService;
import com.rentflow.ai.service.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final EventService eventService;
    private final com.rentflow.security.CurrentUserService currentUserService;

    public CustomerController(CustomerService customerService, EventService eventService,
                              com.rentflow.security.CurrentUserService currentUserService) {
        this.customerService = customerService;
        this.eventService = eventService;
        this.currentUserService = currentUserService;
    }

    private String resolveTenantId(String headerTenantId) {
        if (currentUserService != null && currentUserService.getTenantId().isPresent()) {
            return currentUserService.requireTenantId();
        }
        if (headerTenantId != null && !headerTenantId.isBlank()) {
            return headerTenantId;
        }
        return currentUserService.requireTenantId();
    }

    private String resolveRole(String headerRole) {
        if (currentUserService != null && currentUserService.getRole().isPresent()) {
            return currentUserService.requireRole();
        }
        if (headerRole != null && !headerRole.isBlank()) {
            return headerRole;
        }
        return currentUserService.requireRole();
    }

    private boolean canRead(String role) {
        return List.of("OWNER", "ADMIN", "SALES", "WAREHOUSE", "DRIVER").contains(role);
    }

    private boolean canWrite(String role) {
        return List.of("OWNER", "ADMIN", "SALES").contains(role);
    }

    @GetMapping
    public ResponseEntity<?> getCustomers(
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {

        String role = resolveRole(roleHeader);
        if (!canRead(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You do not have permission to view customers."));
        }

        String tenantId = resolveTenantId(tenantHeader);
        List<CustomerDTO> customers = customerService.getCustomers(tenantId);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchCustomers(
            @RequestParam("query") String query,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {

        String role = resolveRole(roleHeader);
        if (!canRead(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You do not have permission to search customers."));
        }

        if (query == null || query.isBlank()) {
            return ResponseEntity.ok(List.of());
        }
        String sanitizedQuery = query.trim();
        if (sanitizedQuery.length() > 200) {
            sanitizedQuery = sanitizedQuery.substring(0, 200);
        }

        String tenantId = resolveTenantId(tenantHeader);
        List<CustomerDTO> results = customerService.searchCustomers(tenantId, sanitizedQuery);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCustomerById(
            @PathVariable("id") UUID id,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {

        String role = resolveRole(roleHeader);
        if (!canRead(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You do not have permission to view customer details."));
        }

        String tenantId = resolveTenantId(tenantHeader);
        return customerService.getCustomerById(tenantId, id)
                .map(customer -> {
                    // WAREHOUSE / DRIVER limited view filtering if applicable
                    if ("DRIVER".equals(role)) {
                        customer.setNotes("[Hidden for Driver Role]");
                        customer.setBillingAddress("[Hidden for Driver Role]");
                    }
                    return ResponseEntity.ok(customer);
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    public ResponseEntity<?> createCustomer(
            @jakarta.validation.Valid @RequestBody CustomerDTO dto,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {

        String role = resolveRole(roleHeader);
        if (!canWrite(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You do not have permission to create customers."));
        }

        String tenantId = resolveTenantId(tenantHeader);
        CustomerDTO created = customerService.createCustomer(tenantId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCustomer(
            @PathVariable("id") UUID id,
            @jakarta.validation.Valid @RequestBody CustomerDTO dto,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {

        String role = resolveRole(roleHeader);
        if (!canWrite(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You do not have permission to update customer information."));
        }

        String tenantId = resolveTenantId(tenantHeader);
        return customerService.updateCustomer(tenantId, id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/{id}/events")
    public ResponseEntity<?> getCustomerEvents(
            @PathVariable("id") UUID id,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {

        String role = resolveRole(roleHeader);
        if (!canRead(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You do not have permission to view events."));
        }

        String tenantId = resolveTenantId(tenantHeader);
        List<EventDTO> events = eventService.getEventsByCustomer(tenantId, id);
        return ResponseEntity.ok(events);
    }
}
