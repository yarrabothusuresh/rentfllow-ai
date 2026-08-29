package com.rentflow.inventory;

import com.rentflow.ai.model.Product;
import com.rentflow.ai.repository.BookingRepository;
import com.rentflow.ai.repository.CustomerRepository;
import com.rentflow.ai.repository.ProductRepository;
import com.rentflow.inventory.dto.AssetScanResultDTO;
import com.rentflow.inventory.dto.StockAdjustmentDTO;
import com.rentflow.inventory.dto.StockReceiptDTO;
import com.rentflow.inventory.model.*;
import com.rentflow.inventory.repository.InventoryItemRepository;
import com.rentflow.inventory.repository.StockMovementRepository;
import com.rentflow.inventory.repository.WarehouseStockRepository;
import com.rentflow.inventory.service.InventoryService;
import com.rentflow.warehouse.model.Warehouse;
import com.rentflow.warehouse.repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InventoryServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private InventoryItemRepository inventoryItemRepository;
    @Mock private WarehouseStockRepository warehouseStockRepository;
    @Mock private StockMovementRepository stockMovementRepository;
    @Mock private WarehouseRepository warehouseRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private CustomerRepository customerRepository;

    private InventoryService inventoryService;

    private final String TENANT_ID = "tenant-1";
    private Product quantityProduct;
    private Product serializedProduct;
    private Warehouse warehouse;

    @BeforeEach
    void setUp() {
        inventoryService = new InventoryService(
                productRepository, inventoryItemRepository, warehouseStockRepository,
                stockMovementRepository, warehouseRepository, bookingRepository, customerRepository
        );

        quantityProduct = new Product();
        quantityProduct.setId(UUID.randomUUID());
        quantityProduct.setTenantId(TENANT_ID);
        quantityProduct.setSku("WFC-001");
        quantityProduct.setName("White Folding Chair");
        quantityProduct.setTrackingType(ProductTrackingType.QUANTITY);
        quantityProduct.setQuantityOwned(100);

        serializedProduct = new Product();
        serializedProduct.setId(UUID.randomUUID());
        serializedProduct.setTenantId(TENANT_ID);
        serializedProduct.setSku("CHI-001");
        serializedProduct.setName("Chiavari Chair");
        serializedProduct.setTrackingType(ProductTrackingType.SERIALIZED);
        serializedProduct.setQuantityOwned(10);

        warehouse = new Warehouse(UUID.randomUUID(), TENANT_ID, "MAIN", "Main Warehouse", "Dallas TX", 50, 50, 50, true);
    }

    @Test
    void testReceiveStockQuantityProductSuccess() {
        StockReceiptDTO dto = new StockReceiptDTO();
        dto.setProductId(quantityProduct.getId());
        dto.setWarehouseId(warehouse.getId());
        dto.setQuantity(20);

        when(productRepository.findByTenantIdAndId(TENANT_ID, quantityProduct.getId())).thenReturn(Optional.of(quantityProduct));
        when(warehouseRepository.findByTenantIdAndId(TENANT_ID, warehouse.getId())).thenReturn(Optional.of(warehouse));
        when(warehouseStockRepository.findByTenantIdAndProductIdAndWarehouseId(TENANT_ID, quantityProduct.getId(), warehouse.getId()))
                .thenReturn(Optional.empty());
        when(stockMovementRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        List<StockMovement> movements = inventoryService.receiveStock(TENANT_ID, dto, "admin");

        assertNotNull(movements);
        assertEquals(1, movements.size());
        assertEquals(120, quantityProduct.getQuantityOwned());
    }

    @Test
    void testAdjustStockWithoutReasonThrowsException() {
        StockAdjustmentDTO dto = new StockAdjustmentDTO();
        dto.setProductId(quantityProduct.getId());
        dto.setWarehouseId(warehouse.getId());
        dto.setQuantity(5);
        dto.setReason(""); // Empty reason

        assertThrows(ResponseStatusException.class, () -> inventoryService.adjustStock(TENANT_ID, dto, "admin"));
    }

    @Test
    void testScanAssetFound() {
        InventoryItem item = new InventoryItem(
                UUID.randomUUID(), TENANT_ID, serializedProduct.getId(), warehouse.getId(),
                "CHI-000001", "SN-001", "RF-CHI000001", "QR-001",
                AssetStatus.AVAILABLE, AssetCondition.GOOD, BigDecimal.valueOf(100), warehouse.getName()
        );

        when(inventoryItemRepository.findByTenantIdAndAssetCode(TENANT_ID, "CHI-000001")).thenReturn(Optional.of(item));
        when(productRepository.findByTenantIdAndId(TENANT_ID, serializedProduct.getId())).thenReturn(Optional.of(serializedProduct));
        when(warehouseRepository.findByTenantIdAndId(TENANT_ID, warehouse.getId())).thenReturn(Optional.of(warehouse));

        AssetScanResultDTO result = inventoryService.scanAsset(TENANT_ID, "CHI-000001");

        assertTrue(result.isFound());
        assertEquals("CHI-000001", result.getAssetCode());
        assertEquals("Chiavari Chair", result.getProductName());
    }
}
