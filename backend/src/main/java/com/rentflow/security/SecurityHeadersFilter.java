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
 * Enforces production HTTP security headers on all incoming requests.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SecurityHeadersFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Prevent MIME-type sniffing
        response.setHeader("X-Content-Type-Options", "nosniff");

        // 2. Prevent Clickjacking
        response.setHeader("X-Frame-Options", "DENY");

        // 3. Cross-Site Scripting Protection for legacy browsers
        response.setHeader("X-XSS-Protection", "1; mode=block");

        // 4. Referrer Policy
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        // 5. Restrict sensitive browser permissions
        response.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=()");

        // 6. Content Security Policy (Allows self and Google Fonts)
        response.setHeader("Content-Security-Policy",
                "default-src 'self'; " +
                "script-src 'self' 'unsafe-inline'; " +
                "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
                "font-src 'self' https://fonts.gstatic.com; " +
                "img-src 'self' data: https:; " +
                "connect-src 'self' http://localhost:8080 http://localhost:4200;"
        );

        filterChain.doFilter(request, response);
    }
}
