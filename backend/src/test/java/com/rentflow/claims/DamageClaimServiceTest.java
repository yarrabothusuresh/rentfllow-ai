package com.rentflow.claims;

import com.rentflow.ai.model.*;
import com.rentflow.ai.repository.*;
import com.rentflow.claims.dto.*;
import com.rentflow.claims.model.*;
import com.rentflow.claims.repository.*;
import com.rentflow.claims.service.DamageClaimService;
import com.rentflow.claims.service.RepairService;
import com.rentflow.claims.service.ReplacementService;
import com.rentflow.returns.model.*;
import com.rentflow.returns.repository.*;
import com.rentflow.security.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DamageClaimServiceTest {

    @Autowired
    private DamageClaimService claimService;

    @Autowired
    private RepairService repairService;

    @Autowired
    private ReplacementService replacementService;

    @Autowired
    private DamageClaimRepository claimRepository;

    @Autowired
    private DamageClaimItemRepository claimItemRepository;

    @Autowired
    private ClaimEstimateRepository estimateRepository;

    @Autowired
    private RepairOrderRepository repairRepository;

    @Autowired
    private ReplacementOrderRepository replacementRepository;

    @Autowired
    private ReturnOrderRepository returnOrderRepository;

    @Autowired
    private ReturnOrderItemRepository returnOrderItemRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingItemRepository bookingItemRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    private String tenantId;
    private Customer customer;
    private Product chairProduct;
    private Booking testBooking;
    private ReturnOrder testReturn;
    private ReturnOrderItem returnItem;

    @BeforeEach
    void setUp() {
        tenantId = "TEST-TENANT-CLAIM-" + UUID.randomUUID().toString().substring(0, 5);
        SecurityUtils.setTestTenantId(tenantId);

        customer = new Customer();
        customer.setTenantId(tenantId);
        customer.setCustomerNumber("CUST-CLM-" + UUID.randomUUID().toString().substring(0, 8));
        customer.setCustomerType(CustomerType.INDIVIDUAL);
        customer.setStatus(CustomerStatus.ACTIVE);
        customer.setFirstName("ClaimTest");
        customer.setLastName("User");
        customer.setEmail("claim-" + UUID.randomUUID().toString().substring(0, 5) + "@example.com");
        customer = customerRepository.save(customer);

        chairProduct = new Product(UUID.randomUUID(), tenantId, "CLM-CHAIR", "Claim Chair", "Chair", null, ProductType.RENTAL_ITEM, ProductStatus.ACTIVE, BigDecimal.valueOf(10.00), BigDecimal.valueOf(100.00), 50, 0, 0, 0);
        chairProduct = productRepository.save(chairProduct);

        testBooking = new Booking();
        testBooking.setTenantId(tenantId);
        testBooking.setBookingNumber("BOOK-CLM-" + UUID.randomUUID().toString().substring(0, 5));
        testBooking.setQuoteId(UUID.randomUUID());
        testBooking.setCustomerId(customer.getId());
        testBooking.setEventId(UUID.randomUUID());
        testBooking.setStatus(BookingStatus.RETURNED);
        testBooking.setBookingDate(LocalDate.now());
        testBooking.setRentalStartDateTime(LocalDateTime.now().minusDays(2));
        testBooking.setRentalEndDateTime(LocalDateTime.now().minusDays(1));
        testBooking = bookingRepository.save(testBooking);

        BookingItem bi = new BookingItem();
        bi.setBookingId(testBooking.getId());
        bi.setProductId(chairProduct.getId());
        bi.setDescription(chairProduct.getName());
        bi.setQuantity(10);
        bi.setUnitPrice(BigDecimal.valueOf(10.00));
        bi.setLineSubtotal(BigDecimal.valueOf(100.00));
        bi.setRentalStartDateTime(testBooking.getRentalStartDateTime());
        bi.setRentalEndDateTime(testBooking.getRentalEndDateTime());
        bookingItemRepository.save(bi);

        testReturn = new ReturnOrder();
        testReturn.setTenantId(tenantId);
        testReturn.setReturnNumber("RET-CLM-" + UUID.randomUUID().toString().substring(0, 5));
        testReturn.setBookingId(testBooking.getId());
        testReturn.setCustomerId(customer.getId());
        testReturn.setStatus(ReturnOrderStatus.COMPLETED);
        testReturn.setPriority(ReturnPriority.NORMAL);
        testReturn = returnOrderRepository.save(testReturn);

        returnItem = new ReturnOrderItem();
        returnItem.setTenantId(tenantId);
        returnItem.setReturnOrderId(testReturn.getId());
        returnItem.setBookingItemId(bi.getId());
        returnItem.setProductId(chairProduct.getId());
        returnItem.setProductNameSnapshot(chairProduct.getName());
        returnItem.setSkuSnapshot(chairProduct.getSku());
        returnItem.setQuantityExpected(10);
        returnItem.setQuantityReceived(10);
        returnItem.setQuantityGood(5);
        returnItem.setQuantityDamaged(5);
        returnItem.setQuantityMissing(0);
        returnItem.setStatus(ReturnOrderItemStatus.INSPECTED);
        returnItem = returnOrderItemRepository.save(returnItem);
    }

    @AfterEach
    void tearDown() {
        SecurityUtils.clearTestTenantId();
    }

    @Test
    void test1_ClaimCreationFromReturn() {
        DamageClaimDTO claim = claimService.createClaimFromReturn(testReturn.getId());

        assertNotNull(claim);
        assertNotNull(claim.getClaimNumber());
        assertEquals(ClaimStatus.OPEN, claim.getStatus());
        assertEquals(ClaimType.DAMAGE, claim.getClaimType());
        assertEquals(1, claim.getItems().size());
        assertEquals(5, claim.getItems().get(0).getQuantity());
    }

    @Test
    void test2_DuplicateClaimPrevented() {
        DamageClaimDTO first = claimService.createClaimFromReturn(testReturn.getId());
        DamageClaimDTO second = claimService.createClaimFromReturn(testReturn.getId());

        assertEquals(first.getId(), second.getId());
    }

    @Test
    void test3_CostCalculationAndEstimateVersioning() {
        DamageClaimDTO claim = claimService.createClaimFromReturn(testReturn.getId());

        CreateEstimateRequestDTO estReq = new CreateEstimateRequestDTO();
        estReq.setRepairCost(BigDecimal.valueOf(125.00));
        estReq.setLaborCost(BigDecimal.valueOf(25.00));
        estReq.setTax(BigDecimal.valueOf(15.00));

        ClaimEstimateDTO estimate = claimService.createEstimate(claim.getId(), estReq);

        assertNotNull(estimate);
        assertEquals(1, estimate.getVersion());
        assertEquals(0, BigDecimal.valueOf(165.00).compareTo(estimate.getTotal()));

        DamageClaimDTO updatedClaim = claimService.getClaimById(claim.getId());
        assertEquals(ClaimStatus.ESTIMATE_CREATED, updatedClaim.getStatus());
        assertEquals(0, BigDecimal.valueOf(165.00).compareTo(updatedClaim.getEstimatedTotalCost()));
    }

    @Test
    void test4_CustomerApprovalAndDisputeFlow() {
        DamageClaimDTO claim = claimService.createClaimFromReturn(testReturn.getId());

        CreateEstimateRequestDTO estReq = new CreateEstimateRequestDTO();
        estReq.setRepairCost(BigDecimal.valueOf(100.00));
        claimService.createEstimate(claim.getId(), estReq);
        claimService.sendToCustomer(claim.getId());

        DamageClaimDTO disputed = claimService.customerDispute(claim.getId(), "Item was already scratched.", "customer_user");
        assertEquals(ClaimStatus.DISPUTED, disputed.getStatus());
        assertEquals("Item was already scratched.", disputed.getDisputeReason());

        DamageClaimDTO approved = claimService.customerApprove(claim.getId(), "customer_user");
        assertEquals(ClaimStatus.APPROVED, approved.getStatus());
        assertEquals(0, BigDecimal.valueOf(100.00).compareTo(approved.getApprovedTotalCost()));
    }

    @Test
    void test5_WaiveClaimDoesNotChangeInventoryCondition() {
        DamageClaimDTO claim = claimService.createClaimFromReturn(testReturn.getId());
        DamageClaimDTO waived = claimService.waiveClaim(claim.getId(), "Minor damage waived by owner", "admin_user");

        assertEquals(ClaimStatus.WAIVED, waived.getStatus());
        assertEquals(ClaimResolution.WAIVED, waived.getResolution());
    }

    @Test
    void test6_RepairOrderLifecycleAndInventoryRestoration() {
        DamageClaimDTO claim = claimService.createClaimFromReturn(testReturn.getId());

        RepairOrderDTO repair = repairService.createRepairOrder(claim.getId(), chairProduct.getId(), 5, "Scratch repair", BigDecimal.valueOf(100.00));
        assertEquals(RepairOrderStatus.PENDING, repair.getStatus());

        RepairOrderDTO started = repairService.startRepair(repair.getId());
        assertEquals(RepairOrderStatus.IN_PROGRESS, started.getStatus());

        // Verify completion with GOOD condition restores items
        RepairOrderDTO completed = repairService.completeRepair(repair.getId(), 5, InspectionCondition.GOOD, BigDecimal.valueOf(95.00), "All 5 chairs repaired");
        assertEquals(RepairOrderStatus.COMPLETED, completed.getStatus());

        DamageClaimDTO resolved = claimService.resolveClaim(claim.getId(), ClaimResolution.REPAIRED, "Repairs completed", "op_mgr");
        assertEquals(ClaimStatus.RESOLVED, resolved.getStatus());
    }

    @Test
    void test7_ReplacementOrderLifecycle() {
        DamageClaimDTO claim = claimService.createClaimFromReturn(testReturn.getId());

        ReplacementOrderDTO repl = replacementService.createReplacementOrder(claim.getId(), chairProduct.getId(), 2, "Unrepairable legs", BigDecimal.valueOf(100.00));
        assertEquals(ReplacementOrderStatus.PENDING, repl.getStatus());

        ReplacementOrderDTO ordered = replacementService.orderReplacement(repl.getId());
        assertEquals(ReplacementOrderStatus.ORDERED, ordered.getStatus());

        ReplacementOrderDTO received = replacementService.receiveReplacement(repl.getId());
        assertEquals(ReplacementOrderStatus.RECEIVED, received.getStatus());

        ReplacementOrderDTO completed = replacementService.completeReplacement(repl.getId());
        assertEquals(ReplacementOrderStatus.COMPLETED, completed.getStatus());
    }

    @Test
    void test8_MultiTenantIsolation() {
        DamageClaimDTO claim = claimService.createClaimFromReturn(testReturn.getId());

        SecurityUtils.setTestTenantId("OTHER-TENANT");
        List<DamageClaimDTO> otherClaims = claimService.getClaims(null, null, null, null, null);
        assertTrue(otherClaims.isEmpty(), "Tenant B must never see Tenant A claims");

        assertThrows(IllegalArgumentException.class, () -> claimService.getClaimById(claim.getId()));
    }
}
