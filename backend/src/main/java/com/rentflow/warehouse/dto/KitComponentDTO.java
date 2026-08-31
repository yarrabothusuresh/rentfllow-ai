package com.rentflow.warehouse.dto;

import java.util.UUID;

public class KitComponentDTO {
    private UUID id;
    private UUID kitDefinitionId;
    private UUID componentProductId;
    private String componentName;
    private String componentSku;
    private int quantityPerKit;
    private int totalRequiredQuantity;
    private int packedQuantity;
    private boolean satisfied;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

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

    public int getTotalRequiredQuantity() { return totalRequiredQuantity; }
    public void setTotalRequiredQuantity(int totalRequiredQuantity) { this.totalRequiredQuantity = totalRequiredQuantity; }

    public int getPackedQuantity() { return packedQuantity; }
    public void setPackedQuantity(int packedQuantity) { this.packedQuantity = packedQuantity; }

    public boolean isSatisfied() { return satisfied; }
    public void setSatisfied(boolean satisfied) { this.satisfied = satisfied; }
}
