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
    private final com.rentflow.security.CurrentUserService currentUserService;

    public StaffCustomerRequestController(StaffCustomerRequestService staffService,
                                          com.rentflow.security.CurrentUserService currentUserService) {
        this.staffService = staffService;
        this.currentUserService = currentUserService;
    }

    private String resolveTenantId(String ignoredHeader) {
        return currentUserService.requireTenantId();
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
