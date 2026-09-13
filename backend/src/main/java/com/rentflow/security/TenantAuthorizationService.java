package com.rentflow.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

/**
 * Authorization helper for asserting tenant and customer boundaries in business logic.
 */
@Service
public class TenantAuthorizationService {

    private final CurrentUserService currentUserService;

    public TenantAuthorizationService(CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    public String getCurrentTenantId() {
        return currentUserService.requireTenantId();
    }

    public boolean belongsToCurrentTenant(String resourceTenantId) {
        if (resourceTenantId == null) {
            return false;
        }
        return resourceTenantId.equalsIgnoreCase(currentUserService.requireTenantId());
    }

    public void requireSameTenant(String resourceTenantId) {
        if (!belongsToCurrentTenant(resourceTenantId)) {
            throw new AccessDeniedException("Access Denied: Resource does not belong to your tenant.");
        }
    }

    public void requireCustomerOwnership(UUID resourceCustomerId) {
        UUID currentCustomerId = currentUserService.requireCustomerId();
        if (!Objects.equals(currentCustomerId, resourceCustomerId)) {
            throw new AccessDeniedException("Access Denied: Resource does not belong to the authenticated customer.");
        }
    }
}
