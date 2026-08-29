package com.rentflow.inventory.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "inventory_transfer_items", indexes = {
    @Index(name = "idx_tr_item_transfer", columnList = "transferId"),
    @Index(name = "idx_tr_item_product", columnList = "productId"),
    @Index(name = "idx_tr_item_inventory", columnList = "inventoryItemId")
})
public class InventoryTransferItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID transferId;

    @Column(nullable = false)
    private UUID productId;

    private UUID inventoryItemId;

    @Column(nullable = false)
    private int quantity = 1;

    @Column(nullable = false)
    private int receivedQuantity = 0;

    @Enumerated(EnumType.STRING)
    private AssetCondition condition;

    private String notes;

    public InventoryTransferItem() {}

    public InventoryTransferItem(UUID id, UUID transferId, UUID productId, UUID inventoryItemId,
                                 int quantity, int receivedQuantity, AssetCondition condition, String notes) {
        this.id = id != null ? id : UUID.randomUUID();
        this.transferId = transferId;
        this.productId = productId;
        this.inventoryItemId = inventoryItemId;
        this.quantity = quantity;
        this.receivedQuantity = receivedQuantity;
        this.condition = condition;
        this.notes = notes;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getTransferId() { return transferId; }
    public void setTransferId(UUID transferId) { this.transferId = transferId; }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public UUID getInventoryItemId() { return inventoryItemId; }
    public void setInventoryItemId(UUID inventoryItemId) { this.inventoryItemId = inventoryItemId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getReceivedQuantity() { return receivedQuantity; }
    public void setReceivedQuantity(int receivedQuantity) { this.receivedQuantity = receivedQuantity; }

    public AssetCondition getCondition() { return condition; }
    public void setCondition(AssetCondition condition) { this.condition = condition; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
