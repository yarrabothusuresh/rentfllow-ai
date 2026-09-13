package com.rentflow.auth.dto;

import java.util.UUID;

public class UserSummaryDTO {

    private UUID id;
    private String email;
    private String name;
    private String role;
    private String tenantId;

    public UserSummaryDTO() {}

    public UserSummaryDTO(UUID id, String email, String name, String role, String tenantId) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.role = role;
        this.tenantId = tenantId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}
