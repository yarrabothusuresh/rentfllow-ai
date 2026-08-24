package com.rentflow.warehouse.service;

import java.util.Optional;
import java.util.UUID;

public interface InventoryIdentificationService {

    Optional<UUID> resolveProductBySkuOrBarcode(String tenantId, String code);

    Optional<String> resolveLocationByCode(String tenantId, String locationCode);

    boolean verifyItemScan(String tenantId, UUID productId, String scannedCode);
}
