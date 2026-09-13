package com.rentflow.security;

import java.util.Optional;
import java.util.UUID;

/**
 * Centralized service abstraction for accessing the currently authenticated user,
 * tenant, role, and customer identity from the verified Spring Security context.
 *
 * Enforces the Golden Security Rule: never silently fall back to default tenants
 * or default roles. Throws an explicit security exception if the required context is missing.
 */
public interface CurrentUserService {

    /**
     * Returns the verified UUID of the authenticated user.
     * Throws AuthenticationCredentialsNotFoundException if unauthenticated.
     */
    UUID requireUserId();

    /**
     * Returns the verified tenant ID string of the authenticated user.
     * Throws AuthenticationCredentialsNotFoundException if unauthenticated.
     */
    String requireTenantId();

    /**
     * Returns the verified tenant ID converted to UUID.
     * Throws AuthenticationCredentialsNotFoundException or IllegalArgumentException if invalid.
     */
    UUID requireTenantIdAsUuid();

    /**
     * Returns the verified role string (e.g. "OWNER", "ADMIN", "SALES", "CUSTOMER").
     * Throws AuthenticationCredentialsNotFoundException if unauthenticated.
     */
    String requireRole();

    /**
     * Returns the customer ID if the principal is an authenticated customer, or empty otherwise.
     */
    Optional<UUID> getCustomerId();

    /**
     * Returns the customer ID, or throws an AccessDeniedException if the user is not an authenticated customer.
     */
    UUID requireCustomerId();

    /**
     * Returns the underlying verified RentFlowPrincipal.
     * Throws AuthenticationCredentialsNotFoundException if unauthenticated.
     */
    RentFlowPrincipal requirePrincipal();

    /**
     * Returns true if there is currently an authenticated principal.
     */
    boolean isAuthenticated();

    /**
     * Optional accessor for tenant ID if present.
     */
    Optional<String> getTenantId();

    /**
     * Optional accessor for role if present.
     */
    Optional<String> getRole();

    /**
     * Optional accessor for user ID if present.
     */
    Optional<UUID> getUserId();
}
