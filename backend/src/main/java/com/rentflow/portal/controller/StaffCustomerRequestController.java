package com.rentflow.portal.controller;

import com.rentflow.portal.dto.Customer360DTO;
import com.rentflow.portal.dto.StaffCustomerRequestDashboardDTO;
import com.rentflow.portal.service.StaffCustomerRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
public class StaffCustomerRequestController {

    private final StaffCustomerRequestService staffService;

    public StaffCustomerRequestController(StaffCustomerRequestService staffService) {
        this.staffService = staffService;
    }

    private String resolveTenantId(String tenantHeader) {
        return (tenantHeader != null && !tenantHeader.isBlank()) ? tenantHeader : "99999999-9999-9999-9999-999999999999";
    }

    @GetMapping("/api/customer-requests/dashboard")
    public ResponseEntity<StaffCustomerRequestDashboardDTO> getStaffDashboard(
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader) {
        String tenantId = resolveTenantId(tenantHeader);
        return ResponseEntity.ok(staffService.getStaffDashboard(tenantId));
    }

    @GetMapping("/api/customers/{id}/360")
    public ResponseEntity<Customer360DTO> getCustomer360(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader) {
        String tenantId = resolveTenantId(tenantHeader);
        return ResponseEntity.ok(staffService.getCustomer360(tenantId, id));
    }
}
