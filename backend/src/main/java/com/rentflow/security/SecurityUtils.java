package com.rentflow.security;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

/**
 * Security context accessor for RentFlow AI.
 * Obtains identity authoritative from the Spring SecurityContext (RentFlowPrincipal).
 * Unsafe default fallbacks to Evergreen / OWNER have been strictly removed.
 */
public class SecurityUtils {

    private static final ThreadLocal<String> CURRENT_TENANT_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_USER_ROLE = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_USER_NAME = new ThreadLocal<>();

    public static void setTenantId(String tenantId) {
        if (tenantId != null && !tenantId.isBlank()) {
            CURRENT_TENANT_ID.set(tenantId.trim());
        } else {
            CURRENT_TENANT_ID.remove();
        }
    }

    public static void setUserRole(String role) {
        if (role != null && !role.isBlank()) {
            CURRENT_USER_ROLE.set(role.trim().toUpperCase());
        } else {
            CURRENT_USER_ROLE.remove();
        }
    }

    public static void setUserName(String userName) {
        if (userName != null && !userName.isBlank()) {
            CURRENT_USER_NAME.set(userName.trim());
        } else {
            CURRENT_USER_NAME.remove();
        }
    }

    public static void setContext(String tenantId, String userRole, String userName) {
        setTenantId(tenantId);
        setUserRole(userRole);
        setUserName(userName);
    }

    public static void setTestTenantId(String tenantId) {
        setTenantId(tenantId);
        if (CURRENT_USER_NAME.get() == null) {
            CURRENT_USER_NAME.set("test-user");
        }
        if (CURRENT_USER_ROLE.get() == null) {
            CURRENT_USER_ROLE.set("OWNER");
        }
    }

    public static void clearTestTenantId() {
        clearContext();
    }

    public static void clearContext() {
        CURRENT_TENANT_ID.remove();
        CURRENT_USER_ROLE.remove();
        CURRENT_USER_NAME.remove();
    }

    public static boolean hasExplicitTenantContext() {
        if (CURRENT_TENANT_ID.get() != null) {
            return true;
        }
        RentFlowPrincipal principal = getPrincipal();
        return principal != null && principal.getTenantId() != null;
    }

    public static RentFlowPrincipal getPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof RentFlowPrincipal principal) {
            return principal;
        }
        return null;
    }

    /**
     * Retrieves the current authenticated tenant ID.
     * Throws AuthenticationCredentialsNotFoundException if no verified identity exists.
     */
    public static String getCurrentTenantId() {
        RentFlowPrincipal principal = getPrincipal();
        if (principal != null && principal.getTenantId() != null) {
            return principal.getTenantId();
        }
        if (CURRENT_TENANT_ID.get() != null) {
            return CURRENT_TENANT_ID.get();
        }
        throw new AuthenticationCredentialsNotFoundException(
                "Unauthenticated access rejected: no verified tenant context in SecurityContext"
        );
    }

    /**
     * Retrieves the current authenticated user's identifier/email.
     */
    public static String getCurrentUser() {
        RentFlowPrincipal principal = getPrincipal();
        if (principal != null) {
            return principal.getEmail() != null && !principal.getEmail().isBlank()
                    ? principal.getEmail()
                    : principal.getUserId().toString();
        }
        if (CURRENT_USER_NAME.get() != null) {
            return CURRENT_USER_NAME.get();
        }
        throw new AuthenticationCredentialsNotFoundException(
                "Unauthenticated access rejected: no verified user identity in SecurityContext"
        );
    }

    public static String getCurrentUsername() {
        return getCurrentUser();
    }

    public static UUID getCurrentUserId() {
        RentFlowPrincipal principal = getPrincipal();
        if (principal != null) {
            return principal.getUserId();
        }
        throw new AuthenticationCredentialsNotFoundException(
                "Unauthenticated access rejected: no verified userId in SecurityContext"
        );
    }

    public static UUID getCurrentCustomerId() {
        RentFlowPrincipal principal = getPrincipal();
        if (principal != null && principal.getCustomerId() != null) {
            return principal.getCustomerId();
        }
        return null;
    }

    /**
     * Retrieves the current authenticated role.
     */
    public static String getCurrentUserRole() {
        RentFlowPrincipal principal = getPrincipal();
        if (principal != null && principal.getRole() != null) {
            return principal.getRole();
        }
        if (CURRENT_USER_ROLE.get() != null) {
            return CURRENT_USER_ROLE.get();
        }
        throw new AuthenticationCredentialsNotFoundException(
                "Unauthenticated access rejected: no verified user role in SecurityContext"
        );
    }
}
