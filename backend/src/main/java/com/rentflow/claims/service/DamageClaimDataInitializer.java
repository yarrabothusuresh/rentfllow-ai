package com.rentflow.claims.service;

import com.rentflow.ai.model.Booking;
import com.rentflow.ai.model.Customer;
import com.rentflow.ai.model.Event;
import com.rentflow.ai.model.Product;
import com.rentflow.ai.repository.BookingRepository;
import com.rentflow.ai.repository.CustomerRepository;
import com.rentflow.ai.repository.EventRepository;
import com.rentflow.ai.repository.ProductRepository;
import com.rentflow.claims.model.*;
import com.rentflow.claims.repository.*;
import com.rentflow.returns.model.*;
import com.rentflow.returns.repository.*;
import com.rentflow.ai.mock.DemoDataRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@Order(19)
public class DamageClaimDataInitializer implements CommandLineRunner {

    private final DamageClaimRepository claimRepository;
    private final DamageClaimItemRepository claimItemRepository;
    private final ClaimEstimateRepository estimateRepository;
    private final RepairOrderRepository repairRepository;
    private final ReplacementOrderRepository replacementRepository;
    private final ReturnOrderRepository returnOrderRepository;
    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public DamageClaimDataInitializer(DamageClaimRepository claimRepository,
                                     DamageClaimItemRepository claimItemRepository,
                                     ClaimEstimateRepository estimateRepository,
                                     RepairOrderRepository repairRepository,
                                     ReplacementOrderRepository replacementRepository,
                                     ReturnOrderRepository returnOrderRepository,
                                     BookingRepository bookingRepository,
                                     CustomerRepository customerRepository,
                                     ProductRepository productRepository) {
        this.claimRepository = claimRepository;
        this.claimItemRepository = claimItemRepository;
        this.estimateRepository = estimateRepository;
        this.repairRepository = repairRepository;
        this.replacementRepository = replacementRepository;
        this.returnOrderRepository = returnOrderRepository;
        this.bookingRepository = bookingRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        String tenantId = DemoDataRepository.EVERGREEN_TENANT_ID;

        if (claimRepository.countByTenantId(tenantId) > 0) {
            return;
        }

        // Find customer and return order
        List<Customer> customers = customerRepository.findByTenantId(tenantId);
        Customer customer = customers.isEmpty() ? null : customers.get(0);
        final UUID demoCustomerId = customer != null ? customer.getId() : UUID.randomUUID();

        List<ReturnOrder> returns = returnOrderRepository.findByTenantId(tenantId);
        ReturnOrder ret1 = returns.stream().filter(r -> "RET-000123".equals(r.getReturnNumber())).findFirst().orElse(null);
        ReturnOrder ret2 = returns.stream().filter(r -> "RET-000124".equals(r.getReturnNumber())).findFirst().orElse(null);

        UUID ret1Id = ret1 != null ? ret1.getId() : UUID.randomUUID();
        UUID ret2Id = ret2 != null ? ret2.getId() : UUID.randomUUID();
        UUID booking1Id = ret1 != null ? ret1.getBookingId() : UUID.randomUUID();
        UUID booking2Id = ret2 != null ? ret2.getBookingId() : UUID.randomUUID();

        List<Product> products = productRepository.findByTenantId(tenantId);
        Product chairProduct = products.stream().filter(p -> p.getName().toLowerCase().contains("chair")).findFirst().orElse(null);
        Product tableProduct = products.stream().filter(p -> p.getName().toLowerCase().contains("table")).findFirst().orElse(null);
        Product linenProduct = products.stream().filter(p -> p.getName().toLowerCase().contains("linen")).findFirst().orElse(null);

        UUID chairId = chairProduct != null ? chairProduct.getId() : UUID.randomUUID();
        UUID tableId = tableProduct != null ? tableProduct.getId() : UUID.randomUUID();
        UUID linenId = linenProduct != null ? linenProduct.getId() : UUID.randomUUID();

        // ====================================================
        // STEP 47 DEMO DATA: CLM-000123 (5 Damaged Chairs)
        // ====================================================
        DamageClaim claim1 = new DamageClaim();
        claim1.setTenantId(tenantId);
        claim1.setClaimNumber("CLM-000123");
        claim1.setBookingId(booking1Id);
        claim1.setReturnOrderId(ret1Id);
        claim1.setCustomerId(demoCustomerId);
        claim1.setStatus(ClaimStatus.REPAIR_IN_PROGRESS);
        claim1.setClaimType(ClaimType.DAMAGE);
        claim1.setPriority(ReturnPriority.NORMAL);
        claim1.setDescription("5 Chiavari Chairs damaged during rental event");
        claim1.setEstimatedTotalCost(BigDecimal.valueOf(125.00));
        claim1.setApprovedTotalCost(BigDecimal.valueOf(125.00));
        claim1.setReportedBy("Warehouse Manager");
        DamageClaim savedClaim1 = claimRepository.save(claim1);

        DamageClaimItem item1 = new DamageClaimItem();
        item1.setTenantId(tenantId);
        item1.setClaimId(savedClaim1.getId());
        item1.setProductId(chairId);
        item1.setProductNameSnapshot("Chiavari Chair");
        item1.setSkuSnapshot("SKU-CHAIR-01");
        item1.setQuantity(5);
        item1.setClaimType(ClaimType.DAMAGE);
        item1.setCondition(InspectionCondition.MINOR_DAMAGE);
        item1.setDamageCategory(DamageCategory.SCRATCHED);
        item1.setSeverity(DamageSeverity.MAJOR);
        item1.setUnitRepairCost(BigDecimal.valueOf(25.00));
        item1.setUnitReplacementCost(BigDecimal.valueOf(150.00));
        item1.setEstimatedCost(BigDecimal.valueOf(125.00));
        item1.setApprovedCost(BigDecimal.valueOf(125.00));
        claimItemRepository.save(item1);

        ClaimEstimate est1 = new ClaimEstimate();
        est1.setTenantId(tenantId);
        est1.setClaimId(savedClaim1.getId());
        est1.setVersion(1);
        est1.setRepairCost(BigDecimal.valueOf(125.00));
        est1.setReplacementCost(BigDecimal.valueOf(750.00));
        est1.setLaborCost(BigDecimal.valueOf(20.00));
        est1.setTransportCost(BigDecimal.valueOf(15.00));
        est1.setSubtotal(BigDecimal.valueOf(160.00));
        est1.setTotal(BigDecimal.valueOf(160.00));
        est1.setCreatedBy("Operations Manager");
        estimateRepository.save(est1);

        // STEP 47 Repair Order: REP-000123 IN_PROGRESS
        RepairOrder rep1 = new RepairOrder();
        rep1.setTenantId(tenantId);
        rep1.setRepairNumber("REP-000123");
        rep1.setClaimId(savedClaim1.getId());
        rep1.setProductId(chairId);
        rep1.setQuantity(5);
        rep1.setStatus(RepairOrderStatus.IN_PROGRESS);
        rep1.setRepairType("Leg Scratch Refinishing");
        rep1.setDescription("Sanding and re-staining wood finish on 5 chair legs");
        rep1.setEstimatedCost(BigDecimal.valueOf(125.00));
        rep1.setAssignedTo("Warehouse Tech");
        rep1.setStartedAt(LocalDateTime.now().minusHours(4));
        repairRepository.save(rep1);

        // ====================================================
        // STEP 48 DISPUTE DEMO DATA: CLM-000124 (10 Linens Missing)
        // ====================================================
        DamageClaim claim2 = new DamageClaim();
        claim2.setTenantId(tenantId);
        claim2.setClaimNumber("CLM-000124");
        claim2.setBookingId(booking2Id);
        claim2.setReturnOrderId(ret2Id);
        claim2.setCustomerId(demoCustomerId);
        claim2.setStatus(ClaimStatus.DISPUTED);
        claim2.setClaimType(ClaimType.MISSING);
        claim2.setPriority(ReturnPriority.HIGH);
        claim2.setDescription("5 White Linens missing upon check-in");
        claim2.setDisputeReason("These items were returned in the shipment.");
        claim2.setDisputedAt(LocalDateTime.now().minusDays(1));
        claim2.setDisputedBy("Customer Portal");
        claim2.setEstimatedTotalCost(BigDecimal.valueOf(300.00));
        claim2.setReportedBy("Warehouse Inspector");
        DamageClaim savedClaim2 = claimRepository.save(claim2);

        DamageClaimItem item2 = new DamageClaimItem();
        item2.setTenantId(tenantId);
        item2.setClaimId(savedClaim2.getId());
        item2.setProductId(linenId);
        item2.setProductNameSnapshot("White Linen");
        item2.setSkuSnapshot("SKU-LIN-WHT");
        item2.setQuantity(5);
        item2.setClaimType(ClaimType.MISSING);
        item2.setUnitReplacementCost(BigDecimal.valueOf(60.00));
        item2.setEstimatedCost(BigDecimal.valueOf(300.00));
        claimItemRepository.save(item2);

        // ====================================================
        // STEP 50 REPLACEMENT DEMO DATA: RPL-000123
        // ====================================================
        ReplacementOrder rpl1 = new ReplacementOrder();
        rpl1.setTenantId(tenantId);
        rpl1.setReplacementNumber("RPL-000123");
        rpl1.setClaimId(savedClaim2.getId());
        rpl1.setProductId(tableId);
        rpl1.setQuantity(3);
        rpl1.setStatus(ReplacementOrderStatus.ORDERED);
        rpl1.setUnitCost(BigDecimal.valueOf(250.00));
        rpl1.setTotalCost(BigDecimal.valueOf(750.00));
        rpl1.setReason("3 Round Banquet Tables severely broken during event");
        replacementRepository.save(rpl1);
    }
}
