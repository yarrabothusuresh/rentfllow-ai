package com.rentflow.warehouse.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "packing_containers", indexes = {
    @Index(name = "idx_container_tenant", columnList = "tenantId"),
    @Index(name = "idx_container_code", columnList = "containerCode"),
    @Index(name = "idx_container_status", columnList = "status"),
    @Index(name = "idx_container_warehouse", columnList = "warehouseId")
})
public class PackingContainer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private String containerCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContainerType type = ContainerType.CASE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContainerStatus status = ContainerStatus.AVAILABLE;

    private UUID warehouseId;

    private String description;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PackingContainer() {}

    public PackingContainer(UUID id, String tenantId, String containerCode, ContainerType type, ContainerStatus status, UUID warehouseId, String description) {
        this.id = id != null ? id : UUID.randomUUID();
        this.tenantId = tenantId;
        this.containerCode = containerCode;
        this.type = type != null ? type : ContainerType.CASE;
        this.status = status != null ? status : ContainerStatus.AVAILABLE;
        this.warehouseId = warehouseId;
        this.description = description;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (type == null) type = ContainerType.CASE;
        if (status == null) status = ContainerStatus.AVAILABLE;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getContainerCode() { return containerCode; }
    public void setContainerCode(String containerCode) { this.containerCode = containerCode; }

    public ContainerType getType() { return type; }
    public void setType(ContainerType type) { this.type = type; }

    public ContainerStatus getStatus() { return status; }
    public void setStatus(ContainerStatus status) { this.status = status; }

    public UUID getWarehouseId() { return warehouseId; }
    public void setWarehouseId(UUID warehouseId) { this.warehouseId = warehouseId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
