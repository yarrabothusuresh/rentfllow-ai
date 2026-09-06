package com.rentflow.security;

import com.rentflow.ai.model.*;
import com.rentflow.ai.repository.*;
import com.rentflow.automation.model.*;
import com.rentflow.automation.repository.*;
import com.rentflow.claims.model.*;
import com.rentflow.claims.repository.*;
import com.rentflow.crm.model.*;
import com.rentflow.crm.repository.*;
import com.rentflow.delivery.model.*;
import com.rentflow.delivery.repository.*;
import com.rentflow.invoice.model.*;
import com.rentflow.invoice.repository.*;
import com.rentflow.payment.model.*;
import com.rentflow.payment.repository.*;
import com.rentflow.phone.model.*;
import com.rentflow.phone.repository.*;
import com.rentflow.warehouse.model.*;
import com.rentflow.warehouse.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates strict multi-tenant isolation across all core RentFlow platform entities.
 * Tenant A must NEVER be able to query, access, or mutate Tenant B's data.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class Day30CrossTenantSecurityTest {

    private static final String TENANT_A = "11111111-1111-1111-1111-111111111111";
    private static final String TENANT_B = "22222222-2222-2222-2222-222222222222";

    @Autowired private ProductRepository productRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private QuoteRepository quoteRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private InvoiceRepository invoiceRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private DeliveryRepository deliveryRepository;
    @Autowired private DamageClaimRepository damageClaimRepository;
    @Autowired private AiRecommendationRepository recommendationRepository;
    @Autowired private AutomationExecutionRepository executionRepository;
    @Autowired private PhoneCallSessionRepository phoneCallSessionRepository;

    @BeforeEach
    public void setup() {
        SecurityUtils.setTestTenantId(TENANT_A);
    }

    @Test
    @DisplayName("Tenant A cannot access Tenant B's Products")
    public void testProductTenantIsolation() {
        Product prodB = new Product();
        prodB.setTenantId(TENANT_B);
        prodB.setName("Tenant B Exclusive Tent");
        prodB.setSku("SKU-B-TENT");
        prodB.setRentalPrice(BigDecimal.valueOf(500));
        prodB.setQuantityOwned(5);
        Product savedB = productRepository.save(prodB);

        // Query by Tenant A must return empty
        Optional<Product> queriedByA = productRepository.findByTenantIdAndId(TENANT_A, savedB.getId());
        assertTrue(queriedByA.isEmpty(), "Tenant A must not be able to find Tenant B's product by tenant filter");
    }

    @Test
    @DisplayName("Tenant A cannot access Tenant B's Customers")
    public void testCustomerTenantIsolation() {
        Customer custB = new Customer();
        custB.setTenantId(TENANT_B);
        custB.setCustomerNumber("CUST-B-001");
        custB.setFirstName("Secret");
        custB.setLastName("Client");
        custB.setEmail("secret.client@tenantb.com");
        custB.setCustomerType(CustomerType.CORPORATE);
        Customer savedB = customerRepository.save(custB);

        Optional<Customer> queriedByA = customerRepository.findByTenantIdAndId(TENANT_A, savedB.getId());
        assertTrue(queriedByA.isEmpty(), "Tenant A must not access Tenant B's customer");
    }

    @Test
    @DisplayName("Tenant A cannot access Tenant B's Quotes and Bookings")
    public void testQuoteAndBookingTenantIsolation() {
        Quote qB = new Quote();
        qB.setTenantId(TENANT_B);
        qB.setQuoteNumber("Q-B-001");
        qB.setCustomerId(UUID.randomUUID());
        qB.setEventId(UUID.randomUUID());
        qB.setStatus(QuoteStatus.DRAFT);
        qB.setQuoteDate(LocalDate.now());
        qB.setRentalStartDateTime(LocalDateTime.now().plusDays(2));
        qB.setRentalEndDateTime(LocalDateTime.now().plusDays(4));
        qB.setSubtotal(BigDecimal.valueOf(1000));
        qB.setTotalAmount(BigDecimal.valueOf(1000));
        Quote savedQB = quoteRepository.save(qB);

        Booking bB = new Booking();
        bB.setTenantId(TENANT_B);
        bB.setBookingNumber("BK-B-001");
        bB.setQuoteId(savedQB.getId());
        bB.setCustomerId(UUID.randomUUID());
        bB.setEventId(UUID.randomUUID());
        bB.setStatus(BookingStatus.CONFIRMED);
        bB.setBookingDate(LocalDate.now());
        bB.setRentalStartDateTime(LocalDateTime.now().plusDays(2));
        bB.setRentalEndDateTime(LocalDateTime.now().plusDays(4));
        bB.setTotalAmount(BigDecimal.valueOf(1000));
        Booking savedBB = bookingRepository.save(bB);

        assertTrue(quoteRepository.findByTenantIdAndId(TENANT_A, savedQB.getId()).isEmpty(), "Tenant A cannot read Tenant B's quote");
        assertTrue(bookingRepository.findByTenantIdAndId(TENANT_A, savedBB.getId()).isEmpty(), "Tenant A cannot read Tenant B's booking");
    }

    @Test
    @DisplayName("Tenant A cannot access Tenant B's Invoices and Payments")
    public void testInvoiceAndPaymentTenantIsolation() {
        Invoice invB = new Invoice();
        invB.setTenantId(TENANT_B);
        invB.setInvoiceNumber("INV-B-999");
        invB.setCustomerId(UUID.randomUUID());
        invB.setBookingId(UUID.randomUUID());
        invB.setIssueDate(LocalDate.now());
        invB.setDueDate(LocalDate.now().plusDays(14));
        invB.setTotalAmount(BigDecimal.valueOf(2500));
        invB.setBalanceDue(BigDecimal.valueOf(2500));
        invB.setStatus(InvoiceStatus.SENT);
        Invoice savedInvB = invoiceRepository.save(invB);

        Payment payB = new Payment();
        payB.setTenantId(TENANT_B);
        payB.setTransactionReference("PAY-B-999");
        payB.setBookingId(UUID.randomUUID());
        payB.setCustomerId(UUID.randomUUID());
        payB.setAmount(BigDecimal.valueOf(500));
        payB.setPaymentDate(LocalDate.now());
        payB.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        payB.setPaymentStatus(PaymentStatus.COMPLETED);
        Payment savedPayB = paymentRepository.save(payB);

        assertTrue(invoiceRepository.findByTenantIdAndId(TENANT_A, savedInvB.getId()).isEmpty());
        assertTrue(paymentRepository.findByTenantIdAndId(TENANT_A, savedPayB.getId()).isEmpty());
    }

    @Test
    @DisplayName("Tenant A cannot access Tenant B's Deliveries and Damage Claims")
    public void testDeliveryAndDamageClaimTenantIsolation() {
        Delivery delB = new Delivery();
        delB.setTenantId(TENANT_B);
        delB.setDeliveryNumber("DEL-B-100");
        delB.setBookingId(UUID.randomUUID());
        delB.setWarehouseOrderId(UUID.randomUUID());
        delB.setCustomerId(UUID.randomUUID());
        delB.setEventId(UUID.randomUUID());
        delB.setStatus(DeliveryStatus.SCHEDULED);
        Delivery savedDelB = deliveryRepository.save(delB);

        DamageClaim claimB = new DamageClaim();
        claimB.setTenantId(TENANT_B);
        claimB.setClaimNumber("CLM-B-100");
        claimB.setBookingId(UUID.randomUUID());
        claimB.setReturnOrderId(UUID.randomUUID());
        claimB.setCustomerId(UUID.randomUUID());
        claimB.setStatus(ClaimStatus.OPEN);
        claimB.setCurrency("USD");
        DamageClaim savedClaimB = damageClaimRepository.save(claimB);

        assertTrue(deliveryRepository.findByTenantIdAndId(TENANT_A, savedDelB.getId()).isEmpty());
        assertTrue(damageClaimRepository.findByTenantIdAndId(TENANT_A, savedClaimB.getId()).isEmpty());
    }

    @Test
    @DisplayName("Tenant A cannot access Tenant B's AI Recommendations and Automation Executions")
    public void testAiAutomationTenantIsolation() {
        AiRecommendation recB = new AiRecommendation();
        recB.setTenantId(TENANT_B);
        recB.setRecommendationNumber("REC-B-00001");
        recB.setTitle("Tenant B Confidential Recommendation");
        recB.setSummary("High discount alert");
        recB.setCategory(BusinessSignalCategory.PROFITABILITY);
        recB.setPriority(RecommendationPriority.HIGH);
        recB.setSignalType(BusinessSignalType.BOOKING_LOW_MARGIN);
        recB.setSourceEntityType("Booking");
        recB.setSourceEntityId(UUID.randomUUID().toString());
        recB.setEvidenceStrength(EvidenceStrength.HIGH);
        recB.setGeneratedBy(RecommendationGeneratedBy.RULE);
        recB.setStatus(RecommendationStatus.NEW);
        AiRecommendation savedRecB = recommendationRepository.save(recB);

        AutomationExecution execB = new AutomationExecution();
        execB.setTenantId(TENANT_B);
        execB.setIdempotencyKey("EXEC-IDEMP-B-100");
        execB.setActionType(AutomationActionType.CREATE_INTERNAL_TASK);
        execB.setExecutedBy("System");
        execB.setExecutionStatus(AutomationExecutionStatus.EXECUTED);
        AutomationExecution savedExecB = executionRepository.save(execB);

        assertTrue(recommendationRepository.findByIdAndTenantId(savedRecB.getId(), TENANT_A).isEmpty());
        assertTrue(executionRepository.findByTenantIdAndIdempotencyKey(TENANT_A, execB.getIdempotencyKey()).isEmpty());
    }

    @Test
    @DisplayName("Tenant A cannot access Tenant B's Phone Call Sessions")
    public void testPhoneCallSessionTenantIsolation() {
        PhoneCallSession sessionB = new PhoneCallSession();
        sessionB.setTenantId(TENANT_B);
        sessionB.setPublicId("CALL-B-00001");
        sessionB.setProvider("mock");
        sessionB.setDirection(CallDirection.INBOUND);
        sessionB.setStatus(CallStatus.ANSWERED);
        sessionB.setCallerNumberMasked("+1 (555) ***-9999");
        sessionB.setConsentStatus(CallConsentStatus.GRANTED);
        sessionB.setStartedAt(Instant.now());
        PhoneCallSession savedSessionB = phoneCallSessionRepository.save(sessionB);

        assertTrue(phoneCallSessionRepository.findByIdAndTenantId(savedSessionB.getId(), TENANT_A).isEmpty(),
                "Tenant A must not be able to retrieve Tenant B's phone call session");
    }
}
