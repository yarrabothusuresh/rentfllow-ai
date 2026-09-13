package com.rentflow.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of CurrentUserService deriving identity strictly from
 * SecurityUtils and Spring SecurityContextHolder (RentFlowPrincipal).
 */
@Service
public class CurrentUserServiceImpl implements CurrentUserService {

    @Override
    public UUID requireUserId() {
        return SecurityUtils.getCurrentUserId();
    }

    @Override
    public String requireTenantId() {
        return SecurityUtils.getCurrentTenantId();
    }

    @Override
    public UUID requireTenantIdAsUuid() {
        String tenantId = requireTenantId();
        try {
            return UUID.fromString(tenantId);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Current tenant ID is not a valid UUID: " + tenantId, e);
        }
    }

    @Override
    public String requireRole() {
        return SecurityUtils.getCurrentUserRole();
    }

    @Override
    public Optional<UUID> getCustomerId() {
        return Optional.ofNullable(SecurityUtils.getCurrentCustomerId());
    }

    @Override
    public UUID requireCustomerId() {
        return getCustomerId().orElseThrow(() ->
                new AccessDeniedException("Access Denied: Action requires an authenticated customer identity.")
        );
    }

    @Override
    public RentFlowPrincipal requirePrincipal() {
        RentFlowPrincipal principal = SecurityUtils.getPrincipal();
        if (principal == null) {
            throw new AuthenticationCredentialsNotFoundException(
                    "Unauthenticated access rejected: no verified RentFlowPrincipal in SecurityContext"
            );
        }
        return principal;
    }

    @Override
    public boolean isAuthenticated() {
        return SecurityUtils.getPrincipal() != null || SecurityUtils.hasExplicitTenantContext();
    }

    @Override
    public Optional<String> getTenantId() {
        RentFlowPrincipal principal = SecurityUtils.getPrincipal();
        if (principal != null && principal.getTenantId() != null) {
            return Optional.of(principal.getTenantId());
        }
        if (SecurityUtils.hasExplicitTenantContext()) {
            try {
                return Optional.ofNullable(SecurityUtils.getCurrentTenantId());
            } catch (Exception ignored) {}
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getRole() {
        RentFlowPrincipal principal = SecurityUtils.getPrincipal();
        if (principal != null && principal.getRole() != null) {
            return Optional.of(principal.getRole());
        }
        if (SecurityUtils.hasExplicitTenantContext()) {
            try {
                return Optional.ofNullable(SecurityUtils.getCurrentUserRole());
            } catch (Exception ignored) {}
        }
        return Optional.empty();
    }

    @Override
    public Optional<UUID> getUserId() {
        RentFlowPrincipal principal = SecurityUtils.getPrincipal();
        if (principal != null && principal.getUserId() != null) {
            return Optional.of(principal.getUserId());
        }
        if (SecurityUtils.hasExplicitTenantContext()) {
            try {
                return Optional.ofNullable(SecurityUtils.getCurrentUserId());
            } catch (Exception ignored) {}
        }
        return Optional.empty();
    }
}
