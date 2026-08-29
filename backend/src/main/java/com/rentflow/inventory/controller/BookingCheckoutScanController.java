package com.rentflow.inventory.controller;

import com.rentflow.ai.model.Booking;
import com.rentflow.ai.repository.BookingRepository;
import com.rentflow.inventory.dto.AssetScanResultDTO;
import com.rentflow.inventory.model.AssetStatus;
import com.rentflow.inventory.model.InventoryItem;
import com.rentflow.inventory.model.MovementType;
import com.rentflow.inventory.model.StockMovement;
import com.rentflow.inventory.repository.InventoryItemRepository;
import com.rentflow.inventory.repository.StockMovementRepository;
import com.rentflow.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
public class BookingCheckoutScanController {

    private final BookingRepository bookingRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final StockMovementRepository stockMovementRepository;

    public BookingCheckoutScanController(BookingRepository bookingRepository,
                                         InventoryItemRepository inventoryItemRepository,
                                         StockMovementRepository stockMovementRepository) {
        this.bookingRepository = bookingRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    @PostMapping("/{bookingId}/inventory/allocate")
    public ResponseEntity<Map<String, Object>> allocateAssets(@PathVariable UUID bookingId,
                                                               @RequestBody Map<String, Object> body) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        String username = SecurityUtils.getCurrentUsername();

        Booking booking = bookingRepository.findByTenantIdAndId(tenantId, bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found: " + bookingId));

        List<String> assetIds = (List<String>) body.get("assetIds");
        if (assetIds == null || assetIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "assetIds list is required");
        }

        int allocatedCount = 0;
        for (String idStr : assetIds) {
            UUID assetId = UUID.fromString(idStr);
            InventoryItem item = inventoryItemRepository.findByTenantIdAndId(tenantId, assetId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found: " + assetId));

            if (item.getStatus() != AssetStatus.AVAILABLE && item.getStatus() != AssetStatus.RESERVED) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Asset " + item.getAssetCode() + " is not available for allocation");
            }

            item.setStatus(AssetStatus.RESERVED);
            item.setCurrentBookingId(booking.getId());
            inventoryItemRepository.save(item);

            StockMovement sm = new StockMovement(
                    null, tenantId, item.getProductId(), item.getId(), item.getWarehouseId(),
                    MovementType.RESERVATION, 1, null, null, "BOOKING", booking.getId(),
                    "Allocated to booking " + booking.getBookingNumber(), username
            );
            stockMovementRepository.save(sm);
            allocatedCount++;
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "bookingId", booking.getId(),
                "allocatedCount", allocatedCount,
                "message", "Successfully allocated " + allocatedCount + " assets to booking " + booking.getBookingNumber()
        ));
    }

    @PostMapping("/{bookingId}/checkout/scan")
    public ResponseEntity<AssetScanResultDTO> checkoutScan(@PathVariable UUID bookingId,
                                                           @RequestBody Map<String, String> body) {
        String tenantId = SecurityUtils.getCurrentTenantId();
        String username = SecurityUtils.getCurrentUsername();

        Booking booking = bookingRepository.findByTenantIdAndId(tenantId, bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found: " + bookingId));

        String code = body.get("assetCode");
        if (code == null || code.trim().isEmpty()) {
            code = body.get("barcode");
        }
        if (code == null || code.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Asset code or barcode is required");
        }

        String query = code.trim();
        Optional<InventoryItem> itemOpt = inventoryItemRepository.findByTenantIdAndAssetCode(tenantId, query);
        if (itemOpt.isEmpty()) itemOpt = inventoryItemRepository.findByTenantIdAndBarcode(tenantId, query);
        if (itemOpt.isEmpty()) itemOpt = inventoryItemRepository.findByTenantIdAndSerialNumber(tenantId, query);

        if (itemOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No asset found matching scan code: " + query);
        }

        InventoryItem item = itemOpt.get();
        if (item.getStatus() == AssetStatus.OUT_ON_RENT) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Asset " + item.getAssetCode() + " has already been checked out");
        }
        if (item.getStatus() == AssetStatus.DAMAGED || item.getStatus() == AssetStatus.MAINTENANCE || item.getStatus() == AssetStatus.LOST || item.getStatus() == AssetStatus.RETIRED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Asset " + item.getAssetCode() + " is in status " + item.getStatus() + " and cannot be checked out");
        }

        item.setStatus(AssetStatus.OUT_ON_RENT);
        item.setCurrentBookingId(booking.getId());
        inventoryItemRepository.save(item);

        StockMovement sm = new StockMovement(
                null, tenantId, item.getProductId(), item.getId(), item.getWarehouseId(),
                MovementType.CHECKOUT, 1, null, null, "BOOKING", booking.getId(),
                "Checked out asset " + item.getAssetCode() + " for booking " + booking.getBookingNumber(), username
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
        result.setCurrentBookingId(booking.getId());
        result.setCurrentBookingNumber(booking.getBookingNumber());
        result.setMessage("Asset " + item.getAssetCode() + " successfully checked out to booking " + booking.getBookingNumber());

        return ResponseEntity.ok(result);
    }
}
