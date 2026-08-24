package com.rentflow.warehouse.service;

import com.rentflow.ai.model.Product;
import com.rentflow.ai.repository.ProductRepository;
import com.rentflow.warehouse.model.WarehouseLocation;
import com.rentflow.warehouse.repository.WarehouseLocationRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class DefaultInventoryIdentificationServiceImpl implements InventoryIdentificationService {

    private final ProductRepository productRepository;
    private final WarehouseLocationRepository locationRepository;

    public DefaultInventoryIdentificationServiceImpl(ProductRepository productRepository,
                                                       WarehouseLocationRepository locationRepository) {
        this.productRepository = productRepository;
        this.locationRepository = locationRepository;
    }

    @Override
    public Optional<UUID> resolveProductBySkuOrBarcode(String tenantId, String code) {
        if (code == null || code.isBlank()) return Optional.empty();
        return productRepository.findByTenantId(tenantId).stream()
                .filter(p -> code.equalsIgnoreCase(p.getSku()))
                .map(Product::getId)
                .findFirst();
    }

    @Override
    public Optional<String> resolveLocationByCode(String tenantId, String locationCode) {
        if (locationCode == null || locationCode.isBlank()) return Optional.empty();
        return locationRepository.findByTenantIdAndCode(tenantId, locationCode)
                .map(WarehouseLocation::getCode);
    }

    @Override
    public boolean verifyItemScan(String tenantId, UUID productId, String scannedCode) {
        if (productId == null || scannedCode == null) return false;
        return productRepository.findById(productId)
                .map(p -> scannedCode.equalsIgnoreCase(p.getSku()))
                .orElse(false);
    }
}
