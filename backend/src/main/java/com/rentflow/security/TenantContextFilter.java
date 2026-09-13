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
 * Tenant context filter.
 * Derives tenant identity strictly from authenticated JWT context.
 * Raw client headers (X-Tenant-Id, X-User-Role, X-User-Name) are NOT trusted
 * as authoritative identity sources.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class TenantContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            RentFlowPrincipal principal = SecurityUtils.getPrincipal();
            if (principal != null) {
                // Authoritative identity derived directly from authenticated principal
                SecurityUtils.setContext(
                        principal.getTenantId(),
                        principal.getRole(),
                        principal.getEmail()
                );
            }
            filterChain.doFilter(request, response);
        } finally {
            SecurityUtils.clearContext();
        }
    }
}
