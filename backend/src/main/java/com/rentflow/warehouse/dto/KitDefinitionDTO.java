package com.rentflow.warehouse.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class KitDefinitionDTO {
    private UUID id;
    private UUID productId;
    private String name;
    private String description;
    private int orderedKitQuantity;
    private boolean complete;
    private List<KitComponentDTO> components = new ArrayList<>();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getOrderedKitQuantity() { return orderedKitQuantity; }
    public void setOrderedKitQuantity(int orderedKitQuantity) { this.orderedKitQuantity = orderedKitQuantity; }

    public boolean isComplete() { return complete; }
    public void setComplete(boolean complete) { this.complete = complete; }

    public List<KitComponentDTO> getComponents() { return components; }
    public void setItems(List<KitComponentDTO> components) { this.components = components; }
    public void setComponents(List<KitComponentDTO> components) { this.components = components; }
}
