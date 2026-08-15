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
@CrossOrigin(origins = "*")
public class CustomerController {

    private final CustomerService customerService;
    private final EventService eventService;

    public CustomerController(CustomerService customerService, EventService eventService) {
        this.customerService = customerService;
        this.eventService = eventService;
    }

    private String resolveTenantId(String tenantIdHeader) {
        return (tenantIdHeader != null && !tenantIdHeader.isBlank())
                ? tenantIdHeader : DemoDataRepository.EVERGREEN_TENANT_ID;
    }

    private String resolveRole(String roleHeader) {
        return (roleHeader != null && !roleHeader.isBlank())
                ? roleHeader.toUpperCase() : "OWNER";
    }

    private boolean canRead(String role) {
        return List.of("OWNER", "ADMIN", "SALES", "WAREHOUSE", "DRIVER", "CUSTOMER").contains(role);
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

        String tenantId = resolveTenantId(tenantHeader);
        List<CustomerDTO> results = customerService.searchCustomers(tenantId, query);
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
            @RequestBody CustomerDTO dto,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {

        String role = resolveRole(roleHeader);
        if (!canWrite(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You do not have permission to create customers."));
        }

        if (dto.getFirstName() == null || dto.getFirstName().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "First name is required."));
        }
        if (dto.getEmail() == null || !dto.getEmail().contains("@")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Valid email format is required."));
        }

        String tenantId = resolveTenantId(tenantHeader);
        CustomerDTO created = customerService.createCustomer(tenantId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCustomer(
            @PathVariable("id") UUID id,
            @RequestBody CustomerDTO dto,
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
