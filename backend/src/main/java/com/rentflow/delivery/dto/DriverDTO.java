package com.rentflow.delivery.dto;

import com.rentflow.delivery.model.DriverStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public class DriverDTO {
    private UUID id;
    private String tenantId;
    private UUID userId;
    private String name;
    private String phone;
    private String licenseNumber;
    private DriverStatus status;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }
    public DriverStatus getStatus() { return status; }
    public void setStatus(DriverStatus status) { this.status = status; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
