package com.rentflow.ai.service;

import com.rentflow.ai.mock.DemoDataRepository;
import com.rentflow.ai.model.*;
import com.rentflow.ai.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@Order(3) // Run after product/booking seeders
public class InventoryDataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final InventoryReservationRepository reservationRepository;

    public static final UUID DEMO_CHIAVARI_ID = UUID.fromString("00000000-0000-0000-0000-000000000091");
    public static final UUID DEMO_ROUNDTABLE_ID = UUID.fromString("00000000-0000-0000-0000-000000000092");
    public static final UUID DEMO_LINEN_ID = UUID.fromString("00000000-0000-0000-0000-000000000093");

    public InventoryDataInitializer(ProductRepository productRepository,
                                    InventoryReservationRepository reservationRepository) {
        this.productRepository = productRepository;
        this.reservationRepository = reservationRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        String tenantId = DemoDataRepository.EVERGREEN_TENANT_ID;

        // 1. Ensure Chiavari Chair
        Product chiavari = productRepository.findByTenantIdAndId(tenantId, DEMO_CHIAVARI_ID).orElse(null);
        if (chiavari == null) {
            chiavari = new Product();
            chiavari.setId(DEMO_CHIAVARI_ID);
            chiavari.setTenantId(tenantId);
            chiavari.setSku("CHI-001");
            chiavari.setName("Chiavari Chair (Gold)");
            chiavari.setQuantityOwned(200);
            chiavari.setQuantityInMaintenance(0);
            chiavari.setQuantityDamaged(0);
            chiavari.setQuantityLost(0);
            chiavari.setRentalPrice(new BigDecimal("8.00"));
            productRepository.save(chiavari);
        }

        // 2. Ensure Round Table
        Product roundTable = productRepository.findByTenantIdAndId(tenantId, DEMO_ROUNDTABLE_ID).orElse(null);
        if (roundTable == null) {
            roundTable = new Product();
            roundTable.setId(DEMO_ROUNDTABLE_ID);
            roundTable.setTenantId(tenantId);
            roundTable.setSku("TBL-060");
            roundTable.setName("Round Banquet Table 60\"");
            roundTable.setQuantityOwned(50);
            roundTable.setQuantityInMaintenance(0);
            roundTable.setQuantityDamaged(0);
            roundTable.setQuantityLost(0);
            roundTable.setRentalPrice(new BigDecimal("15.00"));
            productRepository.save(roundTable);
        }

        // 3. Ensure White Linen (Total: 500)
        Product linen = productRepository.findByTenantIdAndId(tenantId, DEMO_LINEN_ID).orElse(null);
        if (linen == null) {
            linen = new Product();
            linen.setId(DEMO_LINEN_ID);
            linen.setTenantId(tenantId);
            linen.setSku("LIN-WHT");
            linen.setName("White Table Linen 120\" Round");
            linen.setQuantityOwned(500);
            linen.setQuantityInMaintenance(0);
            linen.setQuantityDamaged(0);
            linen.setQuantityLost(0);
            linen.setRentalPrice(new BigDecimal("10.00"));
            productRepository.save(linen);
        } else {
            if (linen.getQuantityOwned() < 500) {
                linen.setQuantityOwned(500);
                productRepository.save(linen);
            }
        }

        // 4. Seed Demo Reservations for Aug 30, 2026
        LocalDateTime aug30Start = LocalDateTime.of(2026, 8, 30, 8, 0);
        LocalDateTime aug30End = LocalDateTime.of(2026, 8, 30, 22, 0);

        List<InventoryReservation> existingRes = reservationRepository.findByTenantId(tenantId);
        boolean hasAug30Res = existingRes.stream().anyMatch(r -> DEMO_CHIAVARI_ID.equals(r.getProductId()) && r.getQuantity() == 100);

        if (!hasAug30Res) {
            // Booking A reservation: 100 chairs
            InventoryReservation resA = new InventoryReservation();
            resA.setTenantId(tenantId);
            resA.setProductId(DEMO_CHIAVARI_ID);
            resA.setQuantity(100);
            resA.setStartDateTime(aug30Start);
            resA.setEndDateTime(aug30End);
            resA.setStatus(ReservationStatus.RESERVED);
            resA.setReservationType(ReservationType.BOOKING);
            resA.setCreatedBy("System Seeder");
            reservationRepository.save(resA);

            // Booking B reservation: 20 chairs
            InventoryReservation resB = new InventoryReservation();
            resB.setTenantId(tenantId);
            resB.setProductId(DEMO_CHIAVARI_ID);
            resB.setQuantity(20);
            resB.setStartDateTime(aug30Start);
            resB.setEndDateTime(aug30End);
            resB.setStatus(ReservationStatus.RESERVED);
            resB.setReservationType(ReservationType.BOOKING);
            resB.setCreatedBy("System Seeder");
            reservationRepository.save(resB);

            // Round table reservation: 20 tables
            InventoryReservation resTables = new InventoryReservation();
            resTables.setTenantId(tenantId);
            resTables.setProductId(DEMO_ROUNDTABLE_ID);
            resTables.setQuantity(20);
            resTables.setStartDateTime(aug30Start);
            resTables.setEndDateTime(aug30End);
            resTables.setStatus(ReservationStatus.RESERVED);
            resTables.setReservationType(ReservationType.BOOKING);
            resTables.setCreatedBy("System Seeder");
            reservationRepository.save(resTables);

            // Linen reservation: 300 linens
            InventoryReservation resLinen = new InventoryReservation();
            resLinen.setTenantId(tenantId);
            resLinen.setProductId(DEMO_LINEN_ID);
            resLinen.setQuantity(300);
            resLinen.setStartDateTime(aug30Start);
            resLinen.setEndDateTime(aug30End);
            resLinen.setStatus(ReservationStatus.RESERVED);
            resLinen.setReservationType(ReservationType.BOOKING);
            resLinen.setCreatedBy("System Seeder");
            reservationRepository.save(resLinen);
        }
    }
}
