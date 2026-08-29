package com.rentflow.inventory.repository;

import com.rentflow.inventory.model.AssetStatus;
import com.rentflow.inventory.model.InventoryItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, UUID> {

    List<InventoryItem> findByTenantId(String tenantId);

    List<InventoryItem> findByTenantIdAndProductId(String tenantId, UUID productId);

    Page<InventoryItem> findByTenantIdAndProductId(String tenantId, UUID productId, Pageable pageable);

    List<InventoryItem> findByTenantIdAndWarehouseId(String tenantId, UUID warehouseId);

    List<InventoryItem> findByTenantIdAndProductIdAndWarehouseId(String tenantId, UUID productId, UUID warehouseId);

    List<InventoryItem> findByTenantIdAndStatus(String tenantId, AssetStatus status);

    List<InventoryItem> findByTenantIdAndProductIdAndStatus(String tenantId, UUID productId, AssetStatus status);

    Optional<InventoryItem> findByTenantIdAndId(String tenantId, UUID id);

    Optional<InventoryItem> findByTenantIdAndAssetCode(String tenantId, String assetCode);

    Optional<InventoryItem> findByTenantIdAndSerialNumber(String tenantId, String serialNumber);

    Optional<InventoryItem> findByTenantIdAndBarcode(String tenantId, String barcode);

    Optional<InventoryItem> findByTenantIdAndQrCode(String tenantId, String qrCode);

    List<InventoryItem> findByTenantIdAndCurrentBookingId(String tenantId, UUID bookingId);

    long countByTenantIdAndProductIdAndStatus(String tenantId, UUID productId, AssetStatus status);

    long countByTenantIdAndStatus(String tenantId, AssetStatus status);
}
