package com.rentflow.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

/**
 * Authenticated principal representing a verified RentFlow user (staff member or customer).
 * Fully derived from verified JWT tokens or server authentication.
 */
public class RentFlowPrincipal implements UserDetails, Serializable {

    private final UUID userId;
    private final String tenantId;
    private final String email;
    private final String role;
    private final UUID customerId; // Non-null only for CUSTOMER role
    private final boolean active;

    public RentFlowPrincipal(UUID userId, String tenantId, String email, String role, UUID customerId) {
        this(userId, tenantId, email, role, customerId, true);
    }

    public RentFlowPrincipal(UUID userId, String tenantId, String email, String role, UUID customerId, boolean active) {
        this.userId = Objects.requireNonNull(userId, "userId must not be null");
        this.tenantId = Objects.requireNonNull(tenantId, "tenantId must not be null");
        this.email = email != null ? email.trim().toLowerCase() : "";
        this.role = Objects.requireNonNull(role, "role must not be null").toUpperCase();
        this.customerId = customerId;
        this.active = active;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public boolean isCustomer() {
        return "CUSTOMER".equalsIgnoreCase(role) && customerId != null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String authority = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return Collections.singletonList(new SimpleGrantedAuthority(authority));
    }

    @Override
    public String getPassword() {
        return ""; // Credentials are erased post-authentication
    }

    @Override
    public String getUsername() {
        return email != null && !email.isBlank() ? email : userId.toString();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }

    @Override
    public String toString() {
        return "RentFlowPrincipal{" +
                "userId=" + userId +
                ", tenantId='" + tenantId + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", customerId=" + customerId +
                ", active=" + active +
                '}';
    }
}
