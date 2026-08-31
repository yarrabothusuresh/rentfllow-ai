package com.rentflow.warehouse.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "kit_components", indexes = {
    @Index(name = "idx_kit_comp_tenant", columnList = "tenantId"),
    @Index(name = "idx_kit_comp_def", columnList = "kitDefinitionId"),
    @Index(name = "idx_kit_comp_prod", columnList = "componentProductId")
})
public class KitComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private UUID kitDefinitionId;

    @Column(nullable = false)
    private UUID componentProductId;

    @Column(nullable = false)
    private String componentName;

    private String componentSku;

    @Column(nullable = false)
    private int quantityPerKit = 1;

    private LocalDateTime createdAt;

    public KitComponent() {}

    public KitComponent(UUID id, String tenantId, UUID kitDefinitionId, UUID componentProductId, String componentName, String componentSku, int quantityPerKit) {
        this.id = id != null ? id : UUID.randomUUID();
        this.tenantId = tenantId;
        this.kitDefinitionId = kitDefinitionId;
        this.componentProductId = componentProductId;
        this.componentName = componentName;
        this.componentSku = componentSku;
        this.quantityPerKit = quantityPerKit > 0 ? quantityPerKit : 1;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public UUID getKitDefinitionId() { return kitDefinitionId; }
    public void setKitDefinitionId(UUID kitDefinitionId) { this.kitDefinitionId = kitDefinitionId; }

    public UUID getComponentProductId() { return componentProductId; }
    public void setComponentProductId(UUID componentProductId) { this.componentProductId = componentProductId; }

    public String getComponentName() { return componentName; }
    public void setComponentName(String componentName) { this.componentName = componentName; }

    public String getComponentSku() { return componentSku; }
    public void setComponentSku(String componentSku) { this.componentSku = componentSku; }

    public int getQuantityPerKit() { return quantityPerKit; }
    public void setQuantityPerKit(int quantityPerKit) { this.quantityPerKit = quantityPerKit; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
