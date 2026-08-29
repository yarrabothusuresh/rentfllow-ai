package com.rentflow.auth;

import com.rentflow.ai.model.Product;
import com.rentflow.ai.repository.ProductRepository;
import com.rentflow.inventory.model.*;
import com.rentflow.inventory.repository.*;
import com.rentflow.warehouse.model.Warehouse;
import com.rentflow.warehouse.repository.WarehouseRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@Order(22)
public class DataInitializerDay22 implements CommandLineRunner {

    private static final String TENANT_ID = "tenant-1";

    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final WarehouseStockRepository warehouseStockRepository;
    private final StockMovementRepository stockMovementRepository;
    private final InventoryTransferRepository transferRepository;
    private final InventoryTransferItemRepository transferItemRepository;

    public DataInitializerDay22(ProductRepository productRepository,
                               WarehouseRepository warehouseRepository,
                               InventoryItemRepository inventoryItemRepository,
                               WarehouseStockRepository warehouseStockRepository,
                               StockMovementRepository stockMovementRepository,
                               InventoryTransferRepository transferRepository,
                               InventoryTransferItemRepository transferItemRepository) {
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.warehouseStockRepository = warehouseStockRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.transferRepository = transferRepository;
        this.transferItemRepository = transferItemRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!inventoryItemRepository.findByTenantId(TENANT_ID).isEmpty()) {
            return; // Demo data already initialized
        }

        // 1. Ensure Warehouses Exist
        List<Warehouse> warehouses = warehouseRepository.findByTenantId(TENANT_ID);
        Warehouse mainWh = warehouses.stream().filter(w -> "MAIN".equalsIgnoreCase(w.getCode()) || w.getName().contains("Main")).findFirst().orElse(null);
        if (mainWh == null) {
            mainWh = warehouseRepository.save(new Warehouse(null, TENANT_ID, "MAIN", "Main Warehouse", "100 Industry Way, Dallas TX", 50, 50, 50, true));
        }
        Warehouse northWh = warehouseRepository.save(new Warehouse(null, TENANT_ID, "NORTH", "North Warehouse", "450 Logistics Pkwy, Plano TX", 30, 30, 30, true));
        Warehouse southWh = warehouseRepository.save(new Warehouse(null, TENANT_ID, "SOUTH", "South Warehouse", "800 Distribution Blvd, Arlington TX", 20, 20, 20, true));

        // 2. Fetch/Update Products with Tracking Types
        List<Product> products = productRepository.findByTenantId(TENANT_ID);

        Product chiavariChair = products.stream().filter(p -> p.getSku().startsWith("CHI")).findFirst().orElse(null);
        if (chiavariChair != null) {
            chiavariChair.setTrackingType(ProductTrackingType.SERIALIZED);
            chiavariChair.setQuantityOwned(20);
            productRepository.save(chiavariChair);
            seedSerializedAssets(chiavariChair, mainWh, northWh, 20);
        }

        Product foldingChair = products.stream().filter(p -> p.getSku().startsWith("WFC")).findFirst().orElse(null);
        if (foldingChair != null) {
            foldingChair.setTrackingType(ProductTrackingType.QUANTITY);
            foldingChair.setQuantityOwned(200);
            productRepository.save(foldingChair);
            seedQuantityStock(foldingChair, mainWh, northWh, southWh, 120, 50, 30);
        }

        Product roundTable = products.stream().filter(p -> p.getSku().startsWith("TBL-060")).findFirst().orElse(null);
        if (roundTable != null) {
            roundTable.setTrackingType(ProductTrackingType.SERIALIZED);
            roundTable.setQuantityOwned(10);
            productRepository.save(roundTable);
            seedSerializedAssets(roundTable, mainWh, northWh, 10);
        }

        Product whiteLinen = products.stream().filter(p -> p.getSku().startsWith("LIN")).findFirst().orElse(null);
        if (whiteLinen != null) {
            whiteLinen.setTrackingType(ProductTrackingType.QUANTITY);
            whiteLinen.setQuantityOwned(500);
            productRepository.save(whiteLinen);
            seedQuantityStock(whiteLinen, mainWh, northWh, southWh, 300, 150, 50);
        }

        Product lightingKit = products.stream().filter(p -> p.getSku().startsWith("LGT")).findFirst().orElse(null);
        if (lightingKit != null) {
            lightingKit.setTrackingType(ProductTrackingType.SERIALIZED);
            lightingKit.setQuantityOwned(5);
            productRepository.save(lightingKit);
            seedSerializedAssets(lightingKit, mainWh, northWh, 5);
        }

        // 3. Seed Sample Multi-Warehouse Transfer
        InventoryTransfer transfer = new InventoryTransfer(
                null, TENANT_ID, "TRF-2026-001", mainWh.getId(), northWh.getId(),
                TransferStatus.IN_TRANSIT, "warehouse.manager", "Inter-warehouse stock rebalancing"
        );
        transfer.setShippedAt(LocalDateTime.now().minusHours(4));
        transfer = transferRepository.save(transfer);

        if (chiavariChair != null) {
            List<InventoryItem> items = inventoryItemRepository.findByTenantIdAndProductId(TENANT_ID, chiavariChair.getId());
            if (!items.isEmpty()) {
                InventoryTransferItem ti = new InventoryTransferItem(null, transfer.getId(), chiavariChair.getId(), items.get(0).getId(), 1, 0, AssetCondition.GOOD, "Chair transfer");
                transferItemRepository.save(ti);
            }
        }
    }

    private void seedSerializedAssets(Product p, Warehouse wh1, Warehouse wh2, int count) {
        for (int i = 1; i <= count; i++) {
            Warehouse wh = (i % 3 == 0) ? wh2 : wh1;
            String assetCode = p.getSku() + "-" + String.format("%06d", i);
            String barcode = "RF-" + assetCode.replace("-", "");
            String qrToken = "QR-" + UUID.nameUUIDFromBytes(assetCode.getBytes()).toString().substring(0, 8).toUpperCase();
            String serial = "SN-2026-" + p.getSku() + "-" + String.format("%04d", i);

            AssetStatus status = AssetStatus.AVAILABLE;
            AssetCondition condition = AssetCondition.GOOD;
            if (i == count) {
                status = AssetStatus.MAINTENANCE;
                condition = AssetCondition.FAIR;
            } else if (i == count - 1) {
                status = AssetStatus.DAMAGED;
                condition = AssetCondition.DAMAGED;
            }

            InventoryItem item = new InventoryItem(
                    null, TENANT_ID, p.getId(), wh.getId(), assetCode, serial, barcode, qrToken,
                    status, condition, p.getReplacementCost(), wh.getName()
            );
            item = inventoryItemRepository.save(item);

            StockMovement sm = new StockMovement(
                    null, TENANT_ID, p.getId(), item.getId(), wh.getId(), MovementType.RECEIPT,
                    1, null, null, "PO", null, "Initial acquisition receipt", "system"
            );
            stockMovementRepository.save(sm);
        }
    }

    private void seedQuantityStock(Product p, Warehouse wh1, Warehouse wh2, Warehouse wh3, int q1, int q2, int q3) {
        WarehouseStock s1 = new WarehouseStock(null, TENANT_ID, p.getId(), wh1.getId(), q1, q1, 20);
        WarehouseStock s2 = new WarehouseStock(null, TENANT_ID, p.getId(), wh2.getId(), q2, q2, 10);
        WarehouseStock s3 = new WarehouseStock(null, TENANT_ID, p.getId(), wh3.getId(), q3, q3, 5);

        warehouseStockRepository.save(s1);
        warehouseStockRepository.save(s2);
        warehouseStockRepository.save(s3);

        StockMovement sm = new StockMovement(
                null, TENANT_ID, p.getId(), null, wh1.getId(), MovementType.RECEIPT,
                q1 + q2 + q3, null, null, "INITIAL", null, "Initial warehouse stock load", "system"
        );
        stockMovementRepository.save(sm);
    }
}
