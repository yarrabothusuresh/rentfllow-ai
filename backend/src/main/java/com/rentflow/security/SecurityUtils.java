package com.rentflow.security;

import com.rentflow.ai.mock.DemoDataRepository;

/**
 * Thread-safe security context holder for the current HTTP request thread.
 * Holds tenantId, userRole, and userName extracted by TenantContextFilter.
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
        return CURRENT_TENANT_ID.get() != null;
    }

    public static String getCurrentTenantId() {
        if (CURRENT_TENANT_ID.get() != null) {
            return CURRENT_TENANT_ID.get();
        }
        return DemoDataRepository.EVERGREEN_TENANT_ID;
    }

    public static String getCurrentUser() {
        if (CURRENT_USER_NAME.get() != null) {
            return CURRENT_USER_NAME.get();
        }
        return "Operations Manager";
    }

    public static String getCurrentUsername() {
        return getCurrentUser();
    }

    public static String getCurrentUserRole() {
        if (CURRENT_USER_ROLE.get() != null) {
            return CURRENT_USER_ROLE.get();
        }
        return "OWNER";
    }
}
