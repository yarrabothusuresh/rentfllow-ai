package com.rentflow.inventory.service;

import com.rentflow.ai.model.Booking;
import com.rentflow.ai.model.Product;

import com.rentflow.ai.repository.BookingRepository;
import com.rentflow.ai.repository.CustomerRepository;
import com.rentflow.ai.repository.ProductRepository;
import com.rentflow.inventory.dto.*;
import com.rentflow.inventory.model.*;
import com.rentflow.inventory.repository.InventoryItemRepository;
import com.rentflow.inventory.repository.StockMovementRepository;
import com.rentflow.inventory.repository.WarehouseStockRepository;
import com.rentflow.warehouse.model.Warehouse;

import com.rentflow.warehouse.repository.WarehouseRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service("inventory2Service")
public class InventoryService {

    private final ProductRepository productRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final WarehouseStockRepository warehouseStockRepository;
    private final StockMovementRepository stockMovementRepository;
    private final WarehouseRepository warehouseRepository;
    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;

    public InventoryService(ProductRepository productRepository,
                            InventoryItemRepository inventoryItemRepository,
                            WarehouseStockRepository warehouseStockRepository,
                            StockMovementRepository stockMovementRepository,
                            WarehouseRepository warehouseRepository,
                            BookingRepository bookingRepository,
                            CustomerRepository customerRepository) {
        this.productRepository = productRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.warehouseStockRepository = warehouseStockRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.warehouseRepository = warehouseRepository;
        this.bookingRepository = bookingRepository;
        this.customerRepository = customerRepository;
    }

    // ==========================================
    // 1. STOCK RECEIVING
    // ==========================================
    @Transactional
    public List<StockMovement> receiveStock(String tenantId, StockReceiptDTO request, String username) {
        Product product = productRepository.findByTenantIdAndId(tenantId, request.getProductId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        Warehouse warehouse = warehouseRepository.findByTenantIdAndId(tenantId, request.getWarehouseId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Warehouse not found"));

        if (request.getQuantity() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be greater than 0");
        }

        List<StockMovement> movements = new ArrayList<>();

        if (product.getTrackingType() == ProductTrackingType.SERIALIZED) {
            // Serialized Receiving
            for (int i = 0; i < request.getQuantity(); i++) {
                String serialNumber = null;
                if (request.getSerialNumbers() != null && i < request.getSerialNumbers().size()) {
                    serialNumber = request.getSerialNumbers().get(i);
                }
                String assetCode = generateAssetCode(product.getSku(), i + 1);
                String barcode = generateBarcode(assetCode);
                String qrCode = generateQrToken(assetCode);

                InventoryItem item = new InventoryItem(
                        null,
                        tenantId,
                        product.getId(),
                        warehouse.getId(),
                        assetCode,
                        serialNumber,
                        barcode,
                        qrCode,
                        AssetStatus.AVAILABLE,
                        request.getCondition() != null ? request.getCondition() : AssetCondition.NEW,
                        request.getPurchaseCost() != null ? request.getPurchaseCost() : product.getReplacementCost(),
                        warehouse.getName()
                );
                item = inventoryItemRepository.save(item);

                StockMovement movement = new StockMovement(
                        null,
                        tenantId,
                        product.getId(),
                        item.getId(),
                        warehouse.getId(),
                        MovementType.RECEIPT,
                        1,
                        null,
                        null,
                        "RECEIPT",
                        null,
                        request.getNotes() != null ? request.getNotes() : ("Received stock ref: " + request.getReference()),
                        username
                );
                movements.add(stockMovementRepository.save(movement));
            }

            product.setQuantityOwned(product.getQuantityOwned() + request.getQuantity());
            productRepository.save(product);

        } else {
            // Quantity-Based Receiving
            WarehouseStock stock = warehouseStockRepository.findByTenantIdAndProductIdAndWarehouseId(tenantId, product.getId(), warehouse.getId())
                    .orElseGet(() -> new WarehouseStock(null, tenantId, product.getId(), warehouse.getId(), 0, 0, 10));

            stock.setQuantityOnHand(stock.getQuantityOnHand() + request.getQuantity());
            stock.setQuantityAvailable(stock.getQuantityAvailable() + request.getQuantity());
            warehouseStockRepository.save(stock);

            product.setQuantityOwned(product.getQuantityOwned() + request.getQuantity());
            productRepository.save(product);

            StockMovement movement = new StockMovement(
                    null,
                    tenantId,
                    product.getId(),
                    null,
                    warehouse.getId(),
                    MovementType.RECEIPT,
                    request.getQuantity(),
                    null,
                    null,
                    "RECEIPT",
                    null,
                    request.getNotes() != null ? request.getNotes() : ("Received stock ref: " + request.getReference()),
                    username
            );
            movements.add(stockMovementRepository.save(movement));
        }

        return movements;
    }

    // ==========================================
    // 2. STOCK ADJUSTMENT (WITH REASON & APPROVAL THRESHOLD)
    // ==========================================
    @Transactional
    public StockMovement adjustStock(String tenantId, StockAdjustmentDTO request, String username) {
        if (request.getReason() == null || request.getReason().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Adjustment reason is mandatory");
        }

        Product product = productRepository.findByTenantIdAndId(tenantId, request.getProductId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        Warehouse warehouse = warehouseRepository.findByTenantIdAndId(tenantId, request.getWarehouseId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Warehouse not found"));

        MovementType type = request.getType();
        if (type == null) type = MovementType.ADJUSTMENT_IN;

        // If adjustment quantity > 10, require explicit authorization or log high threshold audit
        int qty = Math.abs(request.getQuantity());

        if (product.getTrackingType() == ProductTrackingType.SERIALIZED) {
            if (request.getInventoryItemId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Serialized adjustment requires inventoryItemId");
            }
            InventoryItem item = inventoryItemRepository.findByTenantIdAndId(tenantId, request.getInventoryItemId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory item not found"));

            if (type == MovementType.ADJUSTMENT_OUT || type == MovementType.RETIREMENT || type == MovementType.LOSS) {
                if (item.getStatus() == AssetStatus.OUT_ON_RENT) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot adjust or retire an asset currently OUT_ON_RENT");
                }
                if (type == MovementType.RETIREMENT) item.setStatus(AssetStatus.RETIRED);
                else if (type == MovementType.LOSS) item.setStatus(AssetStatus.LOST);
                else item.setStatus(AssetStatus.RETIRED);
                
                product.setQuantityOwned(Math.max(0, product.getQuantityOwned() - 1));
            } else if (type == MovementType.DAMAGE) {
                item.setStatus(AssetStatus.DAMAGED);
                item.setCondition(AssetCondition.DAMAGED);
                product.setQuantityDamaged(product.getQuantityDamaged() + 1);
            } else if (type == MovementType.RECOVERY) {
                item.setStatus(AssetStatus.AVAILABLE);
                item.setCondition(AssetCondition.GOOD);
                product.setQuantityOwned(product.getQuantityOwned() + 1);
                product.setQuantityLost(Math.max(0, product.getQuantityLost() - 1));
            }
            inventoryItemRepository.save(item);
            productRepository.save(product);

            StockMovement sm = new StockMovement(
                    null, tenantId, product.getId(), item.getId(), warehouse.getId(), type, 1,
                    null, null, "ADJUSTMENT", null, request.getReason() + " (" + (request.getNotes() != null ? request.getNotes() : "") + ")", username
            );
            return stockMovementRepository.save(sm);

        } else {
            // Quantity-Based Adjustment
            WarehouseStock stock = warehouseStockRepository.findByTenantIdAndProductIdAndWarehouseId(tenantId, product.getId(), warehouse.getId())
                    .orElseGet(() -> new WarehouseStock(null, tenantId, product.getId(), warehouse.getId(), 0, 0, 10));

            if (type == MovementType.ADJUSTMENT_IN) {
                stock.setQuantityOnHand(stock.getQuantityOnHand() + qty);
                stock.setQuantityAvailable(stock.getQuantityAvailable() + qty);
                product.setQuantityOwned(product.getQuantityOwned() + qty);
            } else {
                if (stock.getQuantityAvailable() < qty) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient available stock in warehouse for adjustment out");
                }
                stock.setQuantityOnHand(Math.max(0, stock.getQuantityOnHand() - qty));
                stock.setQuantityAvailable(Math.max(0, stock.getQuantityAvailable() - qty));
                product.setQuantityOwned(Math.max(0, product.getQuantityOwned() - qty));

                if (type == MovementType.DAMAGE) {
                    product.setQuantityDamaged(product.getQuantityDamaged() + qty);
                    stock.setQuantityDamaged(stock.getQuantityDamaged() + qty);
                } else if (type == MovementType.LOSS) {
                    product.setQuantityLost(product.getQuantityLost() + qty);
                    stock.setQuantityLost(stock.getQuantityLost() + qty);
                }
            }
            warehouseStockRepository.save(stock);
            productRepository.save(product);

            StockMovement sm = new StockMovement(
                    null, tenantId, product.getId(), null, warehouse.getId(), type, qty,
                    null, null, "ADJUSTMENT", null, request.getReason() + " (" + (request.getNotes() != null ? request.getNotes() : "") + ")", username
            );
            return stockMovementRepository.save(sm);
        }
    }

    // ==========================================
    // 3. BARCODE & QR CODE SCANNING / LOOKUP
    // ==========================================
    public AssetScanResultDTO scanAsset(String tenantId, String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Scan code is required");
        }
        String query = code.trim();

        Optional<InventoryItem> itemOpt = inventoryItemRepository.findByTenantIdAndAssetCode(tenantId, query);
        if (itemOpt.isEmpty()) {
            itemOpt = inventoryItemRepository.findByTenantIdAndBarcode(tenantId, query);
        }
        if (itemOpt.isEmpty()) {
            itemOpt = inventoryItemRepository.findByTenantIdAndSerialNumber(tenantId, query);
        }
        if (itemOpt.isEmpty()) {
            itemOpt = inventoryItemRepository.findByTenantIdAndQrCode(tenantId, query);
        }

        AssetScanResultDTO result = new AssetScanResultDTO();
        if (itemOpt.isEmpty()) {
            result.setFound(false);
            result.setMessage("No physical asset found matching barcode or code: " + query);
            return result;
        }

        InventoryItem item = itemOpt.get();
        result.setFound(true);
        result.setAssetId(item.getId());
        result.setAssetCode(item.getAssetCode());
        result.setSerialNumber(item.getSerialNumber());
        result.setBarcode(item.getBarcode());
        result.setStatus(item.getStatus());
        result.setCondition(item.getCondition());
        result.setWarehouseId(item.getWarehouseId());

        Product product = productRepository.findByTenantIdAndId(tenantId, item.getProductId()).orElse(null);
        if (product != null) {
            result.setProductId(product.getId());
            result.setProductName(product.getName());
            result.setProductSku(product.getSku());
        }

        Warehouse wh = warehouseRepository.findByTenantIdAndId(tenantId, item.getWarehouseId()).orElse(null);
        if (wh != null) {
            result.setWarehouseName(wh.getName());
        }

        if (item.getCurrentBookingId() != null) {
            result.setCurrentBookingId(item.getCurrentBookingId());
            bookingRepository.findByTenantIdAndId(tenantId, item.getCurrentBookingId()).ifPresent(b -> {
                result.setCurrentBookingNumber(b.getBookingNumber());
                if (b.getCustomerId() != null) {
                    customerRepository.findByTenantIdAndId(tenantId, b.getCustomerId())
                            .ifPresent(c -> result.setCustomerName(c.getFirstName() + " " + c.getLastName()));
                }
            });
        }

        result.setMessage("Asset " + item.getAssetCode() + " located successfully");
        return result;
    }

    // ==========================================
    // 4. ASSET DETAIL & HISTORY TIMELINE
    // ==========================================
    public AssetDetailDTO getAssetDetail(String tenantId, UUID assetId) {
        InventoryItem item = inventoryItemRepository.findByTenantIdAndId(tenantId, assetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found: " + assetId));

        AssetDetailDTO dto = new AssetDetailDTO();
        dto.setId(item.getId());
        dto.setProductId(item.getProductId());
        dto.setWarehouseId(item.getWarehouseId());
        dto.setAssetCode(item.getAssetCode());
        dto.setSerialNumber(item.getSerialNumber());
        dto.setBarcode(item.getBarcode());
        dto.setQrCode(item.getQrCode());
        dto.setStatus(item.getStatus());
        dto.setCondition(item.getCondition());
        dto.setAcquisitionDate(item.getAcquisitionDate());
        dto.setPurchaseCost(item.getPurchaseCost());
        dto.setCurrentLocation(item.getCurrentLocation());
        dto.setLastCheckedAt(item.getLastCheckedAt());

        productRepository.findByTenantIdAndId(tenantId, item.getProductId()).ifPresent(p -> {
            dto.setProductName(p.getName());
            dto.setProductSku(p.getSku());
        });

        warehouseRepository.findByTenantIdAndId(tenantId, item.getWarehouseId()).ifPresent(w -> {
            dto.setWarehouseName(w.getName());
        });

        if (item.getCurrentBookingId() != null) {
            dto.setCurrentBookingId(item.getCurrentBookingId());
            bookingRepository.findByTenantIdAndId(tenantId, item.getCurrentBookingId()).ifPresent(b -> {
                dto.setCurrentBookingNumber(b.getBookingNumber());
                if (b.getCustomerId() != null) {
                    customerRepository.findByTenantIdAndId(tenantId, b.getCustomerId())
                            .ifPresent(c -> dto.setCustomerName(c.getFirstName() + " " + c.getLastName()));
                }
            });
        }

        // Fetch history timeline
        List<StockMovement> movements = stockMovementRepository.findByTenantIdAndInventoryItemId(tenantId, item.getId());
        List<AssetDetailDTO.MovementHistoryDTO> history = movements.stream().map(m -> {
            AssetDetailDTO.MovementHistoryDTO h = new AssetDetailDTO.MovementHistoryDTO();
            h.setId(m.getId());
            h.setMovementType(m.getMovementType().name());
            h.setQuantity(m.getQuantity());
            h.setReferenceType(m.getReferenceType());
            h.setReferenceId(m.getReferenceId());
            h.setReason(m.getReason());
            h.setPerformedBy(m.getPerformedBy());
            h.setTimestamp(m.getCreatedAt());

            if (m.getFromWarehouseId() != null) {
                warehouseRepository.findByTenantIdAndId(tenantId, m.getFromWarehouseId()).ifPresent(w -> h.setFromWarehouseName(w.getName()));
            }
            if (m.getToWarehouseId() != null) {
                warehouseRepository.findByTenantIdAndId(tenantId, m.getToWarehouseId()).ifPresent(w -> h.setToWarehouseName(w.getName()));
            }
            return h;
        }).collect(Collectors.toList());

        dto.setHistory(history);
        return dto;
    }

    // ==========================================
    // 5. INVENTORY KPI DASHBOARD SUMMARY
    // ==========================================
    public InventorySummary2DTO getSummary(String tenantId) {
        InventorySummary2DTO summary = new InventorySummary2DTO();

        List<Product> products = productRepository.findByTenantId(tenantId);
        long totalAssets = 0;
        long available = 0;
        long reserved = 0;
        long outOnRent = 0;
        long maint = 0;
        long damaged = 0;
        long lost = 0;
        long retired = 0;
        BigDecimal totalValue = BigDecimal.ZERO;
        long lowStockCount = 0;

        for (Product p : products) {
            BigDecimal cost = p.getReplacementCost() != null ? p.getReplacementCost() : p.getRentalPrice();
            totalValue = totalValue.add(cost.multiply(BigDecimal.valueOf(p.getQuantityOwned())));

            if (p.getTrackingType() == ProductTrackingType.SERIALIZED) {
                List<InventoryItem> items = inventoryItemRepository.findByTenantIdAndProductId(tenantId, p.getId());
                for (InventoryItem item : items) {
                    totalAssets++;
                    switch (item.getStatus()) {
                        case AVAILABLE -> available++;
                        case RESERVED -> reserved++;
                        case OUT_ON_RENT -> outOnRent++;
                        case MAINTENANCE -> maint++;
                        case DAMAGED -> damaged++;
                        case LOST -> lost++;
                        case RETIRED -> retired++;
                        default -> available++;
                    }
                }
            } else {
                totalAssets += p.getQuantityOwned();
                maint += p.getQuantityInMaintenance();
                damaged += p.getQuantityDamaged();
                lost += p.getQuantityLost();
                available += Math.max(0, p.getQuantityOwned() - p.getQuantityInMaintenance() - p.getQuantityDamaged() - p.getQuantityLost());
            }
        }

        summary.setTotalAssets(totalAssets);
        summary.setAvailableAssets(available);
        summary.setReservedAssets(reserved);
        summary.setOutOnRentAssets(outOnRent);
        summary.setMaintenanceAssets(maint);
        summary.setDamagedAssets(damaged);
        summary.setLostAssets(lost);
        summary.setRetiredAssets(retired);
        summary.setTotalAssetValue(totalValue);
        summary.setLowStockCount(lowStockCount);

        return summary;
    }

    // Helper functions for asset codes & barcodes
    private String generateAssetCode(String sku, int seq) {
        String cleanSku = (sku != null ? sku.replaceAll("[^A-Za-z0-9]", "").toUpperCase() : "ITEM");
        return cleanSku + "-" + String.format("%06d", seq);
    }

    private String generateBarcode(String assetCode) {
        return "RF-" + assetCode.replace("-", "");
    }

    private String generateQrToken(String assetCode) {
        return "QR-" + UUID.nameUUIDFromBytes(assetCode.getBytes()).toString().substring(0, 8).toUpperCase();
    }
}
