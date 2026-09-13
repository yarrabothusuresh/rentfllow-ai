package com.rentflow.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Validates incoming Authorization: Bearer <JWT> tokens, establishes authenticated
 * RentFlowPrincipal in Spring SecurityContext, and synchronizes thread-local context.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length()).trim();
            try {
                if (jwtService.isTokenValid(token)) {
                    RentFlowPrincipal principal = jwtService.extractPrincipal(token);
                    if (principal.isEnabled()) {
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        principal,
                                        null,
                                        principal.getAuthorities()
                                );
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        // Also mirror to SecurityUtils ThreadLocal for legacy components
                        SecurityUtils.setContext(
                                principal.getTenantId(),
                                principal.getRole(),
                                principal.getEmail()
                        );
                    }
                }
            } catch (Exception e) {
                // Invalid or expired token: clear context so subsequent checks treat as unauthenticated
                SecurityContextHolder.clearContext();
                SecurityUtils.clearContext();
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            SecurityUtils.clearContext();
        }
    }
}
