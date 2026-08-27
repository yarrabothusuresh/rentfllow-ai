package com.rentflow.security;

import com.rentflow.ai.mock.DemoDataRepository;

public class SecurityUtils {

    private static final ThreadLocal<String> TEST_TENANT_ID = new ThreadLocal<>();

    public static void setTestTenantId(String tenantId) {
        TEST_TENANT_ID.set(tenantId);
    }

    public static void clearTestTenantId() {
        TEST_TENANT_ID.remove();
    }

    public static String getCurrentTenantId() {
        if (TEST_TENANT_ID.get() != null) {
            return TEST_TENANT_ID.get();
        }
        return DemoDataRepository.EVERGREEN_TENANT_ID;
    }

    public static String getCurrentUser() {
        return "Operations Manager";
    }
}
