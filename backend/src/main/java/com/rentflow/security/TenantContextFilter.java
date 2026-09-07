package com.rentflow.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Extracts tenant and user identification headers from incoming HTTP requests
 * and binds them to the current execution thread via SecurityUtils.
 * Guarantees that ThreadLocal context is unconditionally cleared in a finally block.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class TenantContextFilter extends OncePerRequestFilter {

    public static final String HEADER_TENANT_ID = "X-Tenant-Id";
    public static final String HEADER_USER_ROLE = "X-User-Role";
    public static final String HEADER_USER_NAME = "X-User-Name";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String tenantId = request.getHeader(HEADER_TENANT_ID);
        if (tenantId == null || tenantId.isBlank()) {
            tenantId = request.getHeader("X-Tenant-ID");
        }

        String userRole = request.getHeader(HEADER_USER_ROLE);
        String userName = request.getHeader(HEADER_USER_NAME);

        try {
            if (tenantId != null && !tenantId.isBlank()) {
                SecurityUtils.setTenantId(tenantId);
            }
            if (userRole != null && !userRole.isBlank()) {
                SecurityUtils.setUserRole(userRole);
            }
            if (userName != null && !userName.isBlank()) {
                SecurityUtils.setUserName(userName);
            }

            filterChain.doFilter(request, response);
        } finally {
            SecurityUtils.clearContext();
        }
    }
}
