package com.rentflow.inventory.controller;

import com.rentflow.ai.model.Product;
import com.rentflow.ai.repository.ProductRepository;
import com.rentflow.inventory.dto.*;
import com.rentflow.inventory.model.InventoryItem;
import com.rentflow.inventory.model.StockMovement;
import com.rentflow.inventory.repository.InventoryItemRepository;
import com.rentflow.inventory.repository.StockMovementRepository;
import com.rentflow.inventory.service.InventoryService;
import com.rentflow.security.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController("inventory2Controller")
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;
    private final ProductRepository productRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final StockMovementRepository stockMovementRepository;

    public InventoryController(InventoryService inventoryService,
                               ProductRepository productRepository,
                               InventoryItemRepository inventoryItemRepository,
                               StockMovementRepository stockMovementRepository) {
        this.inventoryService = inventoryService;
        this.productRepository = productRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    @GetMapping("/v2/products")
    public ResponseEntity<List<Product>> getInventoryProducts() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(productRepository.findByTenantId(tenantId));
    }

    @GetMapping("/v2/dashboard")
    public ResponseEntity<InventorySummary2DTO> getDashboardSummary() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(inventoryService.getSummary(tenantId));
    }

    @GetMapping("/products/{productId}/assets")
    public ResponseEntity<Page<InventoryItem>> getProductAssets(@PathVariable UUID productId,
                                                                @RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "20") int size) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(inventoryItemRepository.findByTenantIdAndProductId(tenantId, productId, PageRequest.of(page, size, Sort.by("assetCode").ascending())));
    }

    @GetMapping("/assets/{assetId}")
    public ResponseEntity<AssetDetailDTO> getAssetDetail(@PathVariable UUID assetId) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(inventoryService.getAssetDetail(tenantId, assetId));
    }

    @GetMapping("/scan")
    public ResponseEntity<AssetScanResultDTO> scanAsset(@RequestParam String code) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(inventoryService.scanAsset(tenantId, code));
    }

    @GetMapping("/assets/scan/{token}")
    public ResponseEntity<AssetScanResultDTO> scanAssetToken(@PathVariable String token) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(inventoryService.scanAsset(tenantId, token));
    }

    @PostMapping("/receive")
    public ResponseEntity<?> receiveStock(@RequestBody StockReceiptDTO request) {
        String role = SecurityUtils.getCurrentUserRole();
        if (!List.of("OWNER", "ADMIN", "WAREHOUSE").contains(role)) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                    .body(java.util.Map.of("error", "Unauthorized: Only OWNER, ADMIN, and WAREHOUSE roles can receive stock."));
        }
        String tenantId = SecurityUtils.getCurrentTenantId();
        String username = SecurityUtils.getCurrentUsername();
        return ResponseEntity.ok(inventoryService.receiveStock(tenantId, request, username));
    }

    @PostMapping("/adjust")
    public ResponseEntity<?> adjustStock(@RequestBody StockAdjustmentDTO request) {
        String role = SecurityUtils.getCurrentUserRole();
        if (!List.of("OWNER", "ADMIN", "WAREHOUSE").contains(role)) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                    .body(java.util.Map.of("error", "Unauthorized: Only OWNER, ADMIN, and WAREHOUSE roles can adjust stock."));
        }
        String tenantId = SecurityUtils.getCurrentTenantId();
        String username = SecurityUtils.getCurrentUsername();
        return ResponseEntity.ok(inventoryService.adjustStock(tenantId, request, username));
    }

    @GetMapping("/movements")
    public ResponseEntity<Page<StockMovement>> getMovements(@RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "20") int size) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(stockMovementRepository.findByTenantId(tenantId, PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }
}
