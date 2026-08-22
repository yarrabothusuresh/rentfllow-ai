package com.rentflow.ai;

import com.rentflow.ai.dto.*;
import com.rentflow.ai.mock.DemoDataRepository;
import com.rentflow.ai.model.*;
import com.rentflow.ai.orchestrator.AIOrchestrator;
import com.rentflow.ai.repository.*;
import com.rentflow.ai.service.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductInventoryFoundationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductCategoryRepository categoryRepository;

    @Autowired
    private InventoryReservationRepository reservationRepository;

    @Autowired
    private InventoryTransactionRepository transactionRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private AvailabilityService availabilityService;

    @Autowired
    private AIOrchestrator aiOrchestrator;

    private String tenantId;

    @BeforeEach
    void setUp() {
        tenantId = DemoDataRepository.EVERGREEN_TENANT_ID;
        UUID sampleResId = UUID.fromString("77777777-7777-7777-7777-777777777777");
        if (!reservationRepository.existsById(sampleResId)) {
            InventoryReservation res = new InventoryReservation(
                    sampleResId, tenantId, CatalogDataInitializer.CHIAVARI_CHAIR_ID, UUID.fromString("d3b07384-d113-4601-a71f-488667c48564"), null,
                    300, LocalDateTime.of(2026, 9, 20, 10, 0), LocalDateTime.of(2026, 9, 22, 18, 0),
                    ReservationStatus.RESERVED
            );
            reservationRepository.save(res);
        }
    }

    private HttpHeaders createHeaders(String role, String tenant) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Role", role != null ? role : "OWNER");
        headers.set("X-Tenant-Id", tenant != null ? tenant : tenantId);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Test
    void test1_CreateProduct() {
        ProductDTO newProd = new ProductDTO();
        newProd.setSku("TEST-CHAIR-001");
        newProd.setName("Test Banquet Chair");
        newProd.setRentalPrice(new BigDecimal("12.50"));
        newProd.setReplacementCost(new BigDecimal("85.00"));
        newProd.setQuantityOwned(100);
        newProd.setProductType(ProductType.RENTAL_ITEM);
        newProd.setStatus(ProductStatus.ACTIVE);

        HttpEntity<ProductDTO> entity = new HttpEntity<>(newProd, createHeaders("OWNER", tenantId));
        ResponseEntity<ProductDTO> response = restTemplate.postForEntity("/api/products", entity, ProductDTO.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals("TEST-CHAIR-001", response.getBody().getSku());
    }

    @Test
    void test2_SearchProduct() {
        HttpEntity<Void> entity = new HttpEntity<>(createHeaders("SALES", tenantId));
        ResponseEntity<ProductDTO[]> response = restTemplate.exchange(
                "/api/products/search?query=chair", HttpMethod.GET, entity, ProductDTO[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length > 0);
        assertTrue(response.getBody()[0].getName().toLowerCase().contains("chair"));
    }

    @Test
    void test3_AvailabilityCalculationAvailable() {
        // Chiavari Chair: 500 owned, 20 maint, 10 damaged, 5 lost, 300 reserved -> 165 available
        LocalDateTime start = LocalDateTime.of(2026, 9, 20, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 9, 22, 18, 0);

        AvailabilityResultDTO res = availabilityService.checkAvailability(
                tenantId, CatalogDataInitializer.CHIAVARI_CHAIR_ID, 100, start, end);

        assertNotNull(res);
        assertEquals(500, res.getQuantityOwned());
        assertEquals(20, res.getQuantityInMaintenance());
        assertEquals(10, res.getQuantityDamaged());
        assertEquals(5, res.getQuantityLost());
        assertEquals(300, res.getQuantityReserved());
        assertEquals(165, res.getAvailableQuantity());
        assertTrue(res.isAvailable());
        assertEquals(0, res.getShortage());
    }

    @Test
    void test4_AvailabilityCalculationUnavailable() {
        // Request 200 when only 165 available -> unavailable, shortage 35
        LocalDateTime start = LocalDateTime.of(2026, 9, 20, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 9, 22, 18, 0);

        AvailabilityResultDTO res = availabilityService.checkAvailability(
                tenantId, CatalogDataInitializer.CHIAVARI_CHAIR_ID, 200, start, end);

        assertNotNull(res);
        assertEquals(165, res.getAvailableQuantity());
        assertFalse(res.isAvailable());
        assertEquals(35, res.getShortage());
    }

    @Test
    void test5_DateOverlap() {
        // Existing: Sep 20 to Sep 22. Requested: Sep 21 to Sep 23 -> Overlap
        LocalDateTime start = LocalDateTime.of(2026, 9, 21, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 9, 23, 18, 0);

        AvailabilityResultDTO res = availabilityService.checkAvailability(
                tenantId, CatalogDataInitializer.CHIAVARI_CHAIR_ID, 100, start, end);

        assertEquals(300, res.getQuantityReserved());
        assertEquals(1, res.getConflictingReservations().size());
    }

    @Test
    void test6_DateBoundaryNonOverlap() {
        // Existing: Sep 20 to Sep 22 18:00. Requested: Sep 22 18:00 to Sep 24 -> Non-overlapping boundary
        LocalDateTime start = LocalDateTime.of(2026, 9, 22, 18, 0);
        LocalDateTime end = LocalDateTime.of(2026, 9, 24, 18, 0);

        AvailabilityResultDTO res = availabilityService.checkAvailability(
                tenantId, CatalogDataInitializer.CHIAVARI_CHAIR_ID, 100, start, end);

        assertEquals(0, res.getQuantityReserved());
        assertEquals(465, res.getAvailableQuantity());
    }

    @Test
    void test7_TenantIsolation() {
        String tenantB = "tenant-b-other-company";
        HttpEntity<Void> entity = new HttpEntity<>(createHeaders("OWNER", tenantB));
        ResponseEntity<ProductDTO> response = restTemplate.exchange(
                "/api/products/" + CatalogDataInitializer.CHIAVARI_CHAIR_ID,
                HttpMethod.GET, entity, ProductDTO.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void test8_RolePermissionCustomerHidesReplacementCost() {
        HttpEntity<Void> entity = new HttpEntity<>(createHeaders("CUSTOMER", tenantId));
        ResponseEntity<ProductDTO> response = restTemplate.exchange(
                "/api/products/" + CatalogDataInitializer.CHIAVARI_CHAIR_ID,
                HttpMethod.GET, entity, ProductDTO.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().getReplacementCost());
        assertNotNull(response.getBody().getRentalPrice());
    }

    @Test
    void test9_RolePermissionSalesCannotAdjustInventory() {
        InventoryAdjustmentRequest req = new InventoryAdjustmentRequest(10, TransactionType.PURCHASE, "Unauthorized purchase");
        HttpEntity<InventoryAdjustmentRequest> entity = new HttpEntity<>(req, createHeaders("SALES", tenantId));

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/inventory/products/" + CatalogDataInitializer.CHIAVARI_CHAIR_ID + "/adjust",
                entity, String.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void test10_RolePermissionWarehouseCanAdjustInventory() {
        InventoryAdjustmentRequest req = new InventoryAdjustmentRequest(10, TransactionType.PURCHASE, "Warehouse stock arrival");
        HttpEntity<InventoryAdjustmentRequest> entity = new HttpEntity<>(req, createHeaders("WAREHOUSE", tenantId));

        ResponseEntity<ProductDTO> response = restTemplate.postForEntity(
                "/api/inventory/products/" + CatalogDataInitializer.CHIAVARI_CHAIR_ID + "/adjust",
                entity, ProductDTO.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getQuantityOwned() >= 510);
    }

    @Test
    void test11_InventoryAdjustmentCreatesTransaction() {
        InventoryAdjustmentRequest req = new InventoryAdjustmentRequest(5, TransactionType.DAMAGE, "Broken during event return");
        HttpEntity<InventoryAdjustmentRequest> entity = new HttpEntity<>(req, createHeaders("WAREHOUSE", tenantId));

        restTemplate.postForEntity(
                "/api/inventory/products/" + CatalogDataInitializer.WHITE_FOLDING_CHAIR_ID + "/adjust",
                entity, ProductDTO.class);

        HttpEntity<Void> getTxEntity = new HttpEntity<>(createHeaders("WAREHOUSE", tenantId));
        ResponseEntity<InventoryTransactionDTO[]> txs = restTemplate.exchange(
                "/api/inventory/products/" + CatalogDataInitializer.WHITE_FOLDING_CHAIR_ID + "/transactions",
                HttpMethod.GET, getTxEntity, InventoryTransactionDTO[].class);

        assertEquals(HttpStatus.OK, txs.getStatusCode());
        assertNotNull(txs.getBody());
        assertTrue(txs.getBody().length > 0);
        assertEquals(TransactionType.DAMAGE, txs.getBody()[0].getTransactionType());
    }

    @Test
    void test12_ReservationCreatesTransaction() {
        InventoryReservationDTO res = new InventoryReservationDTO();
        res.setProductId(CatalogDataInitializer.ROUND_TABLE_ID);
        res.setQuantity(20);
        res.setStartDateTime(LocalDateTime.of(2026, 10, 1, 10, 0));
        res.setEndDateTime(LocalDateTime.of(2026, 10, 3, 18, 0));

        HttpEntity<InventoryReservationDTO> entity = new HttpEntity<>(res, createHeaders("SALES", tenantId));
        ResponseEntity<InventoryReservationDTO> response = restTemplate.postForEntity(
                "/api/inventory/reservations", entity, InventoryReservationDTO.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());

        List<InventoryTransactionDTO> txs = inventoryService.getTransactions(tenantId, CatalogDataInitializer.ROUND_TABLE_ID);
        assertTrue(txs.stream().anyMatch(t -> t.getTransactionType() == TransactionType.RESERVATION));
    }

    @Test
    void test13_ReleasedReservationRestoresFutureAvailability() {
        InventoryReservationDTO res = new InventoryReservationDTO();
        res.setProductId(CatalogDataInitializer.ROUND_TABLE_ID);
        res.setQuantity(30);
        res.setStartDateTime(LocalDateTime.of(2026, 11, 1, 10, 0));
        res.setEndDateTime(LocalDateTime.of(2026, 11, 3, 18, 0));

        InventoryReservationDTO created = inventoryService.createReservation(tenantId, res, "Sales Test");
        assertNotNull(created.getId());

        // Check availability before release -> 30 reserved
        AvailabilityResultDTO res1 = availabilityService.checkAvailability(
                tenantId, CatalogDataInitializer.ROUND_TABLE_ID, 40,
                LocalDateTime.of(2026, 11, 1, 10, 0), LocalDateTime.of(2026, 11, 3, 18, 0));
        assertEquals(30, res1.getQuantityReserved());

        // Release reservation
        inventoryService.releaseReservation(tenantId, created.getId(), "Sales Test");

        // Check availability after release -> 0 reserved
        AvailabilityResultDTO res2 = availabilityService.checkAvailability(
                tenantId, CatalogDataInitializer.ROUND_TABLE_ID, 40,
                LocalDateTime.of(2026, 11, 1, 10, 0), LocalDateTime.of(2026, 11, 3, 18, 0));
        assertEquals(0, res2.getQuantityReserved());
    }

    @Test
    void test14_CancelledReservationIsExcludedFromAvailability() {
        InventoryReservationDTO res = new InventoryReservationDTO();
        res.setProductId(CatalogDataInitializer.ROUND_TABLE_ID);
        res.setQuantity(25);
        res.setStartDateTime(LocalDateTime.of(2026, 12, 1, 10, 0));
        res.setEndDateTime(LocalDateTime.of(2026, 12, 3, 18, 0));

        InventoryReservationDTO created = inventoryService.createReservation(tenantId, res, "Sales Test");
        inventoryService.cancelReservation(tenantId, created.getId(), "Customer Cancelled");

        AvailabilityResultDTO resCheck = availabilityService.checkAvailability(
                tenantId, CatalogDataInitializer.ROUND_TABLE_ID, 40,
                LocalDateTime.of(2026, 12, 1, 10, 0), LocalDateTime.of(2026, 12, 3, 18, 0));
        assertEquals(0, resCheck.getQuantityReserved());
    }

    @Test
    void test15_AICopilotReservationRequiresApproval() {
        AIRequest request = new AIRequest(
                "Reserve 100 chairs for Emily.",
                "user-sales",
                tenantId,
                "SALES",
                "conv-ai-reserve"
        );

        AIResponse response = aiOrchestrator.processRequest(request);
        assertNotNull(response);
        assertEquals("ACTION_REQUIRES_APPROVAL", response.getIntent());
        assertTrue(response.isRequiresApproval());
    }
}
