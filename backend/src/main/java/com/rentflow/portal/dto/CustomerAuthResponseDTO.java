package com.rentflow.portal.dto;

import java.util.UUID;

public class CustomerAuthResponseDTO {
    private String token;
    private UUID userId;
    private UUID customerId;
    private String tenantId;
    private String email;
    private String customerName;
    private String companyName;
    private String role = "CUSTOMER";

    public CustomerAuthResponseDTO() {}

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
