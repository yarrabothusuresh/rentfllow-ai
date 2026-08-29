package com.rentflow.inventory.controller;

import com.rentflow.inventory.dto.AssetScanResultDTO;
import com.rentflow.inventory.model.AssetCondition;
import com.rentflow.inventory.model.AssetStatus;
import com.rentflow.inventory.model.InventoryItem;
import com.rentflow.inventory.model.MovementType;
import com.rentflow.inventory.model.StockMovement;
import com.rentflow.inventory.repository.InventoryItemRepository;
import com.rentflow.inventory.repository.StockMovementRepository;
import com.rentflow.returns.model.ReturnOrder;
import com.rentflow.returns.repository.ReturnOrderRepository;
import com.rentflow.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/returns")
public class ReturnScanController {

    private final ReturnOrderRepository returnOrderRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final StockMovementRepository stockMovementRepository;

    public ReturnScanController(ReturnOrderRepository returnOrderRepository,
                                InventoryItemRepository inventoryItemRepository,
                                StockMovementRepository stockMovementRepository) {
        this.returnOrderRepository = returnOrderRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    @PostMapping("/{returnId}/scan")
    public ResponseEntity<AssetScanResultDTO> scanReturnAsset(@PathVariable UUID returnId,
                                                              @RequestBody Map<String, String> body) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        String username = SecurityUtils.getCurrentUsername();

        ReturnOrder returnOrder = returnOrderRepository.findByTenantIdAndId(tenantId, returnId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Return order not found: " + returnId));

        String code = body.get("assetCode");
        if (code == null || code.trim().isEmpty()) code = body.get("barcode");
        if (code == null || code.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Asset code or barcode is required");
        }

        String conditionStr = body.get("condition");
        AssetCondition condition = AssetCondition.GOOD;
        if (conditionStr != null) {
            try {
                condition = AssetCondition.valueOf(conditionStr.toUpperCase());
            } catch (Exception ignored) {}
        }

        String query = code.trim();
        Optional<InventoryItem> itemOpt = inventoryItemRepository.findByTenantIdAndAssetCode(tenantId, query);
        if (itemOpt.isEmpty()) itemOpt = inventoryItemRepository.findByTenantIdAndBarcode(tenantId, query);

        if (itemOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No asset found matching scan code: " + query);
        }

        InventoryItem item = itemOpt.get();
        item.setCondition(condition);

        if (condition == AssetCondition.DAMAGED) {
            item.setStatus(AssetStatus.DAMAGED);
        } else if (condition == AssetCondition.UNUSABLE) {
            item.setStatus(AssetStatus.MAINTENANCE);
        } else {
            item.setStatus(AssetStatus.INSPECTION);
        }

        item.setCurrentBookingId(null);
        inventoryItemRepository.save(item);

        StockMovement sm = new StockMovement(
                null, tenantId, item.getProductId(), item.getId(), item.getWarehouseId(),
                MovementType.RETURN, 1, null, null, "RETURN", returnOrder.getId(),
                "Returned asset " + item.getAssetCode() + " condition: " + condition, username
        );
        stockMovementRepository.save(sm);

        AssetScanResultDTO result = new AssetScanResultDTO();
        result.setFound(true);
        result.setAssetId(item.getId());
        result.setAssetCode(item.getAssetCode());
        result.setSerialNumber(item.getSerialNumber());
        result.setBarcode(item.getBarcode());
        result.setStatus(item.getStatus());
        result.setCondition(item.getCondition());
        result.setWarehouseId(item.getWarehouseId());
        result.setProductId(item.getProductId());
        result.setMessage("Asset " + item.getAssetCode() + " scanned for return order " + returnOrder.getReturnNumber() + " with condition " + condition);

        return ResponseEntity.ok(result);
    }
}
