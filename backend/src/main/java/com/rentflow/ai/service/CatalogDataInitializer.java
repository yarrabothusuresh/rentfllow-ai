package com.rentflow.ai.service;

import com.rentflow.ai.mock.DemoDataRepository;
import com.rentflow.ai.model.*;
import com.rentflow.ai.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.core.annotation.Order;

@Component
@Order(1)
public class CatalogDataInitializer implements CommandLineRunner {

    private final ProductCategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final InventoryReservationRepository reservationRepository;
    private final InventoryTransactionRepository transactionRepository;

    public static final UUID CHIAVARI_CHAIR_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final UUID WHITE_FOLDING_CHAIR_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    public static final UUID ROUND_TABLE_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    public CatalogDataInitializer(ProductCategoryRepository categoryRepository,
                                  ProductRepository productRepository,
                                  InventoryReservationRepository reservationRepository,
                                  InventoryTransactionRepository transactionRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.reservationRepository = reservationRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        String tenantId = DemoDataRepository.EVERGREEN_TENANT_ID;

        if (categoryRepository.count() > 0 && productRepository.findById(CHIAVARI_CHAIR_ID).isPresent()) {
            return;
        }
        initCategoriesAndProducts(tenantId);
    }

    private void initCategoriesAndProducts(String tenantId) {
        // Parent Categories
        ProductCategory furniture = new ProductCategory(UUID.fromString("c0000000-0000-0000-0000-000000000001"), tenantId, "Furniture", "Event furniture and seating", null);
        ProductCategory linensCat = new ProductCategory(UUID.fromString("c0000000-0000-0000-0000-000000000002"), tenantId, "Linens", "Table linens and drapes", null);
        ProductCategory lightingCat = new ProductCategory(UUID.fromString("c0000000-0000-0000-0000-000000000003"), tenantId, "Lighting", "Stage and ambient lighting", null);
        ProductCategory tentsCat = new ProductCategory(UUID.fromString("c0000000-0000-0000-0000-000000000004"), tenantId, "Tents", "Outdoor event tents", null);
        ProductCategory stagingCat = new ProductCategory(UUID.fromString("c0000000-0000-0000-0000-000000000005"), tenantId, "Staging", "Platform stages and risers", null);
        ProductCategory danceCat = new ProductCategory(UUID.fromString("c0000000-0000-0000-0000-000000000006"), tenantId, "Dance Floors", "Portable dance floors", null);
        ProductCategory packagesCat = new ProductCategory(UUID.fromString("c0000000-0000-0000-0000-000000000007"), tenantId, "Packages", "Event rental packages", null);

        categoryRepository.saveAll(List.of(furniture, linensCat, lightingCat, tentsCat, stagingCat, danceCat, packagesCat));

        // Subcategories
        ProductCategory chairs = new ProductCategory(UUID.fromString("c0000000-0000-0000-0000-000000000010"), tenantId, "Chairs", "Seating solutions", furniture.getId());
        ProductCategory tables = new ProductCategory(UUID.fromString("c0000000-0000-0000-0000-000000000011"), tenantId, "Tables", "Event tables", furniture.getId());
        categoryRepository.saveAll(List.of(chairs, tables));

        // Seed Demo Products
        Product chiavari = new Product(
                CHIAVARI_CHAIR_ID, tenantId, "CHI-001", "Gold Chiavari Chair",
                "Elegant gold Chiavari chair with plush cushion", chairs.getId(),
                ProductType.RENTAL_ITEM, ProductStatus.ACTIVE,
                new BigDecimal("8.00"), new BigDecimal("65.00"),
                500, 20, 10, 5
        );
        chiavari.setImageUrl("https://images.unsplash.com/photo-1503602642458-232111445657?w=500&q=80");

        Product whiteFolding = new Product(
                WHITE_FOLDING_CHAIR_ID, tenantId, "WFC-002", "White Folding Chair",
                "Durable resin white folding chair for ceremonies", chairs.getId(),
                ProductType.RENTAL_ITEM, ProductStatus.ACTIVE,
                new BigDecimal("3.50"), new BigDecimal("30.00"),
                800, 10, 5, 0
        );
        whiteFolding.setImageUrl("https://images.unsplash.com/photo-1586023492125-27b2c045efd7?w=500&q=80");

        Product roundTable = new Product(
                ROUND_TABLE_ID, tenantId, "TBL-060", "60-inch Round Table",
                "60-inch round wood folding table (Seats 8-10)", tables.getId(),
                ProductType.RENTAL_ITEM, ProductStatus.ACTIVE,
                new BigDecimal("14.00"), new BigDecimal("120.00"),
                60, 2, 1, 0
        );
        roundTable.setImageUrl("https://images.unsplash.com/photo-1615066390971-03e4e1c36ddf?w=500&q=80");

        Product banquetTable = new Product(
                UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"), tenantId, "TBL-BNQ6", "6-foot Banquet Table",
                "6-foot rectangular folding banquet table (Seats 6-8)", tables.getId(),
                ProductType.RENTAL_ITEM, ProductStatus.ACTIVE,
                new BigDecimal("12.00"), new BigDecimal("100.00"),
                50, 0, 0, 0
        );
        banquetTable.setImageUrl("https://images.unsplash.com/photo-1577140917170-285929fb55b7?w=500&q=80");

        Product cocktailTable = new Product(
                UUID.fromString("44444444-4444-4444-4444-444444444444"), tenantId, "TBL-CKT", "Cocktail Table",
                "30-inch round high-top bistro cocktail table", tables.getId(),
                ProductType.RENTAL_ITEM, ProductStatus.ACTIVE,
                new BigDecimal("12.00"), new BigDecimal("95.00"),
                40, 1, 0, 0
        );
        cocktailTable.setImageUrl("https://images.unsplash.com/photo-1530018607912-eff2daa1bac4?w=500&q=80");

        Product linen = new Product(
                UUID.fromString("55555555-5555-5555-5555-555555555555"), tenantId, "LIN-WHT", "White Table Linen",
                "120-inch round white polyester table linen", linensCat.getId(),
                ProductType.RENTAL_ITEM, ProductStatus.ACTIVE,
                new BigDecimal("10.00"), new BigDecimal("45.00"),
                185, 3, 2, 0
        );
        linen.setImageUrl("https://images.unsplash.com/photo-1519741497674-611481863552?w=500&q=80");

        Product ivoryLinen = new Product(
                UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"), tenantId, "LIN-IVR", "Ivory Table Linen",
                "120-inch round ivory polyester table linen", linensCat.getId(),
                ProductType.RENTAL_ITEM, ProductStatus.ACTIVE,
                new BigDecimal("10.00"), new BigDecimal("45.00"),
                100, 2, 0, 0
        );
        ivoryLinen.setImageUrl("https://images.unsplash.com/photo-1519741497674-611481863552?w=500&q=80");

        Product uplight = new Product(
                UUID.fromString("66666666-6666-6666-6666-666666666666"), tenantId, "LGT-LED", "LED Uplight",
                "Wireless RGBAW LED battery-powered wash light", lightingCat.getId(),
                ProductType.RENTAL_ITEM, ProductStatus.ACTIVE,
                new BigDecimal("25.00"), new BigDecimal("180.00"),
                50, 3, 1, 0
        );
        uplight.setImageUrl("https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=500&q=80");

        Product bistroChair = new Product(
                UUID.fromString("77777777-7777-7777-7777-777777777777"), tenantId, "CHR-BST", "Bistro Chair",
                "Cross-back wooden bistro chair", chairs.getId(),
                ProductType.RENTAL_ITEM, ProductStatus.ACTIVE,
                new BigDecimal("6.50"), new BigDecimal("50.00"),
                200, 5, 2, 0
        );
        bistroChair.setImageUrl("https://images.unsplash.com/photo-1567538096630-e0c55bd6374c?w=500&q=80");

        Product tent = new Product(
                UUID.fromString("88888888-8888-8888-8888-888888888888"), tenantId, "TNT-2020", "20x20 Tent",
                "20ft x 20ft high peak frame tent", tentsCat.getId(),
                ProductType.RENTAL_ITEM, ProductStatus.ACTIVE,
                new BigDecimal("350.00"), new BigDecimal("2500.00"),
                10, 1, 0, 0
        );
        tent.setImageUrl("https://images.unsplash.com/photo-1464366400600-7168b8af9bc3?w=500&q=80");

        Product danceFloor = new Product(
                UUID.fromString("99999999-9999-9999-9999-999999999999"), tenantId, "DNC-FLR", "Dance Floor",
                "Oak wood grain modular dance floor 15x15ft", danceCat.getId(),
                ProductType.RENTAL_ITEM, ProductStatus.ACTIVE,
                new BigDecimal("450.00"), new BigDecimal("3200.00"),
                8, 0, 0, 0
        );
        danceFloor.setImageUrl("https://images.unsplash.com/photo-1545128485-c400e7702796?w=500&q=80");

        Product stage = new Product(
                UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"), tenantId, "STG-PLT", "Stage Platform",
                "4x8ft heavy duty portable stage riser platform", stagingCat.getId(),
                ProductType.RENTAL_ITEM, ProductStatus.ACTIVE,
                new BigDecimal("75.00"), new BigDecimal("600.00"),
                15, 1, 0, 0
        );
        stage.setImageUrl("https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=500&q=80");

        List<Product> allSeeded = List.of(
                chiavari, whiteFolding, roundTable, banquetTable, cocktailTable,
                linen, ivoryLinen, uplight, bistroChair, tent, danceFloor, stage
        );
        productRepository.saveAll(allSeeded);

        // Initial Purchase Transaction Logs
        for (Product p : allSeeded) {
            InventoryTransaction tx = new InventoryTransaction(
                    UUID.randomUUID(), tenantId, p.getId(), TransactionType.PURCHASE,
                    p.getQuantityOwned(), "INITIAL_PURCHASE", null,
                    "Initial stock inventory allocation", "System"
            );
            transactionRepository.save(tx);
        }

        // Seed Sample Overlapping Reservation for Emily's Wedding (Sep 20, 2026 10:00 to Sep 22, 2026 18:00)
        UUID emilyEventId = UUID.fromString("d3b07384-d113-4601-a71f-488667c48564");
        InventoryReservation res = new InventoryReservation(
                UUID.randomUUID(), tenantId, chiavari.getId(), emilyEventId, null,
                300, LocalDateTime.of(2026, 9, 20, 10, 0), LocalDateTime.of(2026, 9, 22, 18, 0),
                ReservationStatus.RESERVED
        );
        reservationRepository.save(res);

        InventoryTransaction resTx = new InventoryTransaction(
                UUID.randomUUID(), tenantId, chiavari.getId(), TransactionType.RESERVATION,
                300, "EVENT", emilyEventId, "Reserved for Emily's Wedding Reception", "Sales Team"
        );
        transactionRepository.save(resTx);
    }
}
