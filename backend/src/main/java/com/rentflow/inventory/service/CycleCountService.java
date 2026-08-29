package com.rentflow.inventory.service;

import com.rentflow.ai.model.Product;
import com.rentflow.ai.repository.ProductRepository;
import com.rentflow.inventory.dto.CycleCountRequestDTO;
import com.rentflow.inventory.dto.StockAdjustmentDTO;
import com.rentflow.inventory.model.*;
import com.rentflow.inventory.repository.*;
import com.rentflow.warehouse.repository.WarehouseRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CycleCountService {

    private final InventoryCycleCountRepository cycleCountRepository;
    private final InventoryCycleCountItemRepository cycleCountItemRepository;
    private final ProductRepository productRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final WarehouseStockRepository warehouseStockRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryService inventoryService;

    public CycleCountService(InventoryCycleCountRepository cycleCountRepository,
                             InventoryCycleCountItemRepository cycleCountItemRepository,
                             ProductRepository productRepository,
                             InventoryItemRepository inventoryItemRepository,
                             WarehouseStockRepository warehouseStockRepository,
                             WarehouseRepository warehouseRepository,
                             InventoryService inventoryService) {
        this.cycleCountRepository = cycleCountRepository;
        this.cycleCountItemRepository = cycleCountItemRepository;
        this.productRepository = productRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.warehouseStockRepository = warehouseStockRepository;
        this.warehouseRepository = warehouseRepository;
        this.inventoryService = inventoryService;
    }

    // 1. CREATE CYCLE COUNT
    @Transactional
    public InventoryCycleCount createCycleCount(String tenantId, CycleCountRequestDTO request, String username) {
        warehouseRepository.findByTenantIdAndId(tenantId, request.getWarehouseId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Warehouse not found"));

        String num = "CC-" + System.currentTimeMillis() % 1000000;
        InventoryCycleCount cc = new InventoryCycleCount(
                null, tenantId, num, request.getWarehouseId(), request.getCategoryId(),
                LocalDateTime.now(), request.getAssignedTo(), request.getNotes()
        );
        cc = cycleCountRepository.save(cc);

        List<Product> products = productRepository.findByTenantId(tenantId);
        if (request.getCategoryId() != null) {
            products = products.stream().filter(p -> request.getCategoryId().equals(p.getCategoryId())).collect(Collectors.toList());
        }

        for (Product p : products) {
            if (p.getTrackingType() == ProductTrackingType.SERIALIZED) {
                List<InventoryItem> items = inventoryItemRepository.findByTenantIdAndProductIdAndWarehouseId(tenantId, p.getId(), request.getWarehouseId());
                for (InventoryItem item : items) {
                    InventoryCycleCountItem cci = new InventoryCycleCountItem(null, cc.getId(), p.getId(), item.getId(), 1, 0, null, null);
                    cycleCountItemRepository.save(cci);
                }
            } else {
                WarehouseStock stock = warehouseStockRepository.findByTenantIdAndProductIdAndWarehouseId(tenantId, p.getId(), request.getWarehouseId()).orElse(null);
                int expected = stock != null ? stock.getQuantityOnHand() : 0;
                InventoryCycleCountItem cci = new InventoryCycleCountItem(null, cc.getId(), p.getId(), null, expected, 0, null, null);
                cycleCountItemRepository.save(cci);
            }
        }

        return cc;
    }

    // 2. RECORD PHYSICAL COUNTS
    @Transactional
    public InventoryCycleCount recordCounts(String tenantId, UUID cycleCountId, List<CycleCountRequestDTO.CountItemDTO> itemCounts) {
        InventoryCycleCount cc = cycleCountRepository.findByTenantIdAndId(tenantId, cycleCountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cycle count not found: " + cycleCountId));

        if (cc.getStatus() == CycleCountStatus.APPROVED || cc.getStatus() == CycleCountStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot update count in status: " + cc.getStatus());
        }

        cc.setStatus(CycleCountStatus.IN_PROGRESS);

        List<InventoryCycleCountItem> items = cycleCountItemRepository.findByCycleCountId(cc.getId());
        Map<UUID, InventoryCycleCountItem> itemMap = items.stream().collect(Collectors.toMap(InventoryCycleCountItem::getId, i -> i, (a, b) -> a));

        if (itemCounts != null) {
            for (CycleCountRequestDTO.CountItemDTO c : itemCounts) {
                Optional<InventoryCycleCountItem> target = items.stream().filter(i ->
                        (i.getInventoryItemId() != null && i.getInventoryItemId().equals(c.getInventoryItemId())) ||
                        (i.getProductId().equals(c.getProductId()) && i.getInventoryItemId() == null)
                ).findFirst();

                if (target.isPresent()) {
                    InventoryCycleCountItem cci = target.get();
                    cci.setCountedQuantity(c.getCountedQuantity());
                    if (c.getReason() != null) cci.setReason(c.getReason());
                    if (c.getNotes() != null) cci.setNotes(c.getNotes());
                    cycleCountItemRepository.save(cci);
                }
            }
        }

        return cycleCountRepository.save(cc);
    }

    // 3. COMPLETE CYCLE COUNT
    @Transactional
    public InventoryCycleCount completeCycleCount(String tenantId, UUID cycleCountId) {
        InventoryCycleCount cc = cycleCountRepository.findByTenantIdAndId(tenantId, cycleCountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cycle count not found"));
        cc.setStatus(CycleCountStatus.COMPLETED);
        return cycleCountRepository.save(cc);
    }

    // 4. APPROVE CYCLE COUNT & APPLY ADJUSTMENTS
    @Transactional
    public InventoryCycleCount approveCycleCount(String tenantId, UUID cycleCountId, String username) {
        InventoryCycleCount cc = cycleCountRepository.findByTenantIdAndId(tenantId, cycleCountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cycle count not found"));

        if (cc.getStatus() != CycleCountStatus.COMPLETED && cc.getStatus() != CycleCountStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cycle count must be COMPLETED before approval");
        }

        List<InventoryCycleCountItem> items = cycleCountItemRepository.findByCycleCountId(cc.getId());
        for (InventoryCycleCountItem item : items) {
            if (item.getVariance() != 0) {
                StockAdjustmentDTO adj = new StockAdjustmentDTO();
                adj.setProductId(item.getProductId());
                adj.setWarehouseId(cc.getWarehouseId());
                adj.setInventoryItemId(item.getInventoryItemId());
                adj.setQuantity(Math.abs(item.getVariance()));
                adj.setType(item.getVariance() > 0 ? MovementType.ADJUSTMENT_IN : MovementType.ADJUSTMENT_OUT);
                adj.setReason("Cycle Count Correction " + cc.getCountNumber() + ": " + (item.getReason() != null ? item.getReason() : "Variance correction"));

                inventoryService.adjustStock(tenantId, adj, username);
            }
        }

        cc.setStatus(CycleCountStatus.APPROVED);
        cc.setApprovedBy(username);
        return cycleCountRepository.save(cc);
    }

    public List<InventoryCycleCount> getCycleCounts(String tenantId) {
        return cycleCountRepository.findByTenantId(tenantId);
    }

    public InventoryCycleCount getById(String tenantId, UUID id) {
        return cycleCountRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cycle count not found: " + id));
    }

    public List<InventoryCycleCountItem> getItems(UUID cycleCountId) {
        return cycleCountItemRepository.findByCycleCountId(cycleCountId);
    }
}
