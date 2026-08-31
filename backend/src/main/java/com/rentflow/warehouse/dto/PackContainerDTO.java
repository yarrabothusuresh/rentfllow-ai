package com.rentflow.warehouse.dto;

import com.rentflow.warehouse.model.ContainerStatus;
import com.rentflow.warehouse.model.ContainerType;
import java.time.LocalDateTime;
import java.util.UUID;

public class PackContainerDTO {
    private UUID id;
    private String tenantId;
    private String containerCode;
    private ContainerType type;
    private ContainerStatus status;
    private UUID warehouseId;
    private String description;
    private int currentItemCount;
    private LocalDateTime createdAt;

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

    public int getCurrentItemCount() { return currentItemCount; }
    public void setCurrentItemCount(int currentItemCount) { this.currentItemCount = currentItemCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
