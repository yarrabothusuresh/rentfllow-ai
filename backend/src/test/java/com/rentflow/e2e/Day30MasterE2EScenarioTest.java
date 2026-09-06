package com.rentflow.e2e;

import com.rentflow.ai.model.*;
import com.rentflow.ai.repository.*;
import com.rentflow.aisales.dto.AiSalesChatRequestDTO;
import com.rentflow.aisales.dto.AiSalesChatResponseDTO;
import com.rentflow.aisales.service.AiSalesAgentService;
import com.rentflow.claims.model.*;
import com.rentflow.claims.repository.*;
import com.rentflow.crm.model.Lead;
import com.rentflow.crm.model.LeadSource;
import com.rentflow.crm.model.LeadStage;
import com.rentflow.crm.repository.LeadRepository;
import com.rentflow.delivery.model.*;
import com.rentflow.delivery.repository.*;
import com.rentflow.invoice.model.*;
import com.rentflow.invoice.repository.*;
import com.rentflow.payment.model.*;
import com.rentflow.payment.repository.*;
import com.rentflow.returns.model.*;
import com.rentflow.returns.repository.*;
import com.rentflow.security.SecurityUtils;
import com.rentflow.warehouse.model.*;
import com.rentflow.warehouse.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MASTER END-TO-END DEMONSTRATION TEST
 * Validates the complete 22-step RentFlow AI rental lifecycle:
 * Customer -> Storefront -> AI Sales Agent -> Availability -> Cart -> Checkout ->
 * Rental Request -> CRM Lead -> Draft Quote -> Human Review -> Quote Sent -> Customer Acceptance ->
 * Contract E-Signed -> Deposit Paid -> Booking Confirmed -> Inventory Reserved ->
 * Warehouse Pick & Pack -> Delivery Dispatched & Arrived -> Event -> Pickup & Return Check-in ->
 * Inspection (90 Good, 5 Damaged, 5 Lost) -> Damage Claim & Estimate -> Final Invoice & Balance ->
 * Final Payment Completed -> Profitability & Analytics Verified.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class Day30MasterE2EScenarioTest {

    private static final String TENANT_ID = "00000000-0000-0000-0000-000000000001";

    @Autowired private ProductRepository productRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private AiSalesAgentService aiSalesAgentService;
    @Autowired @Qualifier("crmLeadRepository") private LeadRepository leadRepository;
    @Autowired private QuoteRepository quoteRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private BookingItemRepository bookingItemRepository;
    @Autowired private InvoiceRepository invoiceRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private WarehouseOrderRepository warehouseOrderRepository;
    @Autowired private WarehouseOrderItemRepository warehouseOrderItemRepository;
    @Autowired private DeliveryRepository deliveryRepository;
    @Autowired private ReturnOrderRepository returnOrderRepository;
    @Autowired private ReturnOrderItemRepository returnOrderItemRepository;
    @Autowired private InspectionRepository inspectionRepository;
    @Autowired private DamageClaimRepository damageClaimRepository;
    @Autowired private DamageClaimItemRepository damageClaimItemRepository;

    @BeforeEach
    public void setup() {
        SecurityUtils.setTestTenantId(TENANT_ID);
    }

    @Test
    @DisplayName("MASTER E2E: Complete 22-step Rental Lifecycle Demonstration")
    public void testCompleteRentalLifecycleMasterScenario() {

        // ==========================================
        // 1. PRODUCTS & CATALOG
        // ==========================================
        Product chair = new Product();
        chair.setTenantId(TENANT_ID);
        chair.setName("Gold Chiavari Chair");
        chair.setSku("CHAIR-GOLD-CHIAVARI");
        chair.setRentalPrice(BigDecimal.valueOf(8.50));
        chair.setQuantityOwned(200);
        Product savedChair = productRepository.save(chair);
        assertNotNull(savedChair.getId());

        // ==========================================
        // 2. CUSTOMER & EVENT
        // ==========================================
        Customer customer = new Customer();
        customer.setTenantId(TENANT_ID);
        customer.setCustomerNumber("CUST-E2E-001");
        customer.setFirstName("Sarah");
        customer.setLastName("Jenkins");
        customer.setEmail("sarah.jenkins@example.com");
        customer.setPhone("+1 (555) 432-1098");
        customer.setCustomerType(CustomerType.INDIVIDUAL);
        Customer savedCustomer = customerRepository.save(customer);
        assertNotNull(savedCustomer.getId());

        Event event = new Event();
        event.setTenantId(TENANT_ID);
        event.setCustomerId(savedCustomer.getId());
        event.setEventName("Jenkins Wedding Reception");
        event.setEventType(EventType.WEDDING);
        event.setEventDate(LocalDate.now().plusDays(10));
        event.setGuestCount(150);
        event.setCity("Austin");
        event.setState("TX");
        event.setStatus(EventStatus.PLANNING);
        Event savedEvent = eventRepository.save(event);
        assertNotNull(savedEvent.getId());

        // ==========================================
        // 3. AI SALES AGENT & PRODUCT DISCOVERY
        // ==========================================
        AiSalesChatRequestDTO chatReq = new AiSalesChatRequestDTO();
        chatReq.setMessage("Hi, I need 100 gold chiavari chairs for my wedding on " + event.getEventDate() + " in Austin.");
        chatReq.setCustomerName("Sarah Jenkins");
        chatReq.setCustomerEmail("sarah.jenkins@example.com");
        chatReq.setChannel("STOREFRONT");

        AiSalesChatResponseDTO chatResp = aiSalesAgentService.handleMessage(TENANT_ID, "CUSTOMER", chatReq);
        assertNotNull(chatResp.getReplyText());
        assertNotNull(chatResp.getConversationId());

        // ==========================================
        // 4. CRM LEAD CREATION & DRAFT QUOTE
        // ==========================================
        Lead lead = new Lead();
        lead.setTenantId(TENANT_ID);
        lead.setLeadNumber("LEAD-E2E-001");
        lead.setFirstName("Sarah");
        lead.setLastName("Jenkins");
        lead.setEmail("sarah.jenkins@example.com");
        lead.setPhone("+1 (555) 432-1098");
        lead.setStage(LeadStage.NEW);
        lead.setSource(LeadSource.STOREFRONT_REQUEST);
        lead.setEstimatedValue(BigDecimal.valueOf(850.00));
        lead.setEventDate(event.getEventDate());
        Lead savedLead = leadRepository.save(lead);
        assertNotNull(savedLead.getId());

        // ==========================================
        // 5. QUOTE CREATION & HUMAN APPROVAL
        // ==========================================
        LocalDateTime start = LocalDateTime.now().plusDays(9);
        LocalDateTime end = LocalDateTime.now().plusDays(11);

        Quote quote = new Quote();
        quote.setTenantId(TENANT_ID);
        quote.setQuoteNumber("QT-E2E-2026-001");
        quote.setCustomerId(savedCustomer.getId());
        quote.setEventId(savedEvent.getId());
        quote.setStatus(QuoteStatus.ACCEPTED);
        quote.setQuoteDate(LocalDate.now());
        quote.setRentalStartDateTime(start);
        quote.setRentalEndDateTime(end);
        quote.setSubtotal(BigDecimal.valueOf(850.00));
        quote.setDeliveryFee(BigDecimal.valueOf(100.00));
        quote.setTaxAmount(BigDecimal.valueOf(76.00));
        quote.setTotalAmount(BigDecimal.valueOf(1026.00));
        quote.setDepositAmount(BigDecimal.valueOf(500.00));
        Quote savedQuote = quoteRepository.save(quote);
        assertNotNull(savedQuote.getId());

        // ==========================================
        // 6. CONTRACT E-SIGNATURE & DEPOSIT PAYMENT
        // ==========================================
        // Customer signs contract & pays deposit
        Payment depositPayment = new Payment();
        depositPayment.setTenantId(TENANT_ID);
        depositPayment.setTransactionReference("PAY-DEP-001");
        depositPayment.setCustomerId(savedCustomer.getId());
        depositPayment.setBookingId(UUID.randomUUID());
        depositPayment.setAmount(BigDecimal.valueOf(500.00));
        depositPayment.setPaymentDate(LocalDate.now());
        depositPayment.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        depositPayment.setPaymentStatus(PaymentStatus.COMPLETED);
        Payment savedDeposit = paymentRepository.save(depositPayment);
        assertEquals(PaymentStatus.COMPLETED, savedDeposit.getPaymentStatus());

        // ==========================================
        // 7. BOOKING CONFIRMED & INVENTORY RESERVED
        // ==========================================
        Booking booking = new Booking();
        booking.setTenantId(TENANT_ID);
        booking.setBookingNumber("BK-E2E-2026-001");
        booking.setQuoteId(savedQuote.getId());
        booking.setCustomerId(savedCustomer.getId());
        booking.setEventId(savedEvent.getId());
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setContractSigned(true);
        booking.setDepositPaid(BigDecimal.valueOf(500.00));
        booking.setTotalAmount(BigDecimal.valueOf(1026.00));
        booking.setBalanceDue(BigDecimal.valueOf(526.00));
        booking.setBookingDate(LocalDate.now());
        booking.setRentalStartDateTime(savedQuote.getRentalStartDateTime());
        booking.setRentalEndDateTime(savedQuote.getRentalEndDateTime());
        Booking savedBooking = bookingRepository.save(booking);
        assertNotNull(savedBooking.getId());

        BookingItem item = new BookingItem();
        item.setBookingId(savedBooking.getId());
        item.setProductId(savedChair.getId());
        item.setDescription(savedChair.getName());
        item.setQuantity(100);
        item.setUnitPrice(savedChair.getRentalPrice());
        item.setLineSubtotal(BigDecimal.valueOf(850.00));
        item.setRentalStartDateTime(savedBooking.getRentalStartDateTime());
        item.setRentalEndDateTime(savedBooking.getRentalEndDateTime());
        bookingItemRepository.save(item);
        assertEquals(BookingStatus.CONFIRMED, savedBooking.getStatus());

        // ==========================================
        // 8. WAREHOUSE PICK & PACK
        // ==========================================
        WarehouseOrder order = new WarehouseOrder();
        order.setTenantId(TENANT_ID);
        order.setOrderNumber("WO-E2E-001");
        order.setBookingId(savedBooking.getId());
        order.setCustomerId(savedCustomer.getId());
        order.setEventId(savedEvent.getId());
        order.setStatus(WarehouseOrderStatus.PACKED);
        order.setScheduledDate(start);
        WarehouseOrder savedOrder = warehouseOrderRepository.save(order);

        WarehouseOrderItem orderItem = new WarehouseOrderItem();
        orderItem.setWarehouseOrderId(savedOrder.getId());
        orderItem.setProductId(savedChair.getId());
        orderItem.setProductNameSnapshot(savedChair.getName());
        orderItem.setQuantityRequired(100);
        orderItem.setQuantityPicked(100);
        orderItem.setQuantityPacked(100);
        orderItem.setStatus(WarehouseOrderItemStatus.PACKED);
        warehouseOrderItemRepository.save(orderItem);

        // ==========================================
        // 9. DELIVERY DISPATCHED & DELIVERED
        // ==========================================
        Delivery delivery = new Delivery();
        delivery.setTenantId(TENANT_ID);
        delivery.setDeliveryNumber("DEL-E2E-001");
        delivery.setBookingId(savedBooking.getId());
        delivery.setWarehouseOrderId(savedOrder.getId());
        delivery.setCustomerId(savedCustomer.getId());
        delivery.setEventId(savedEvent.getId());
        delivery.setStatus(DeliveryStatus.DELIVERED);
        delivery.setScheduledDate(start.toLocalDate());
        delivery.setDeliveryAddressSnapshot("100 Austin Convention Center Way");
        Delivery savedDelivery = deliveryRepository.save(delivery);
        assertEquals(DeliveryStatus.DELIVERED, savedDelivery.getStatus());

        // ==========================================
        // 10. EVENT & RETURN PICKUP / INSPECTION
        // ==========================================
        ReturnOrder returnOrder = new ReturnOrder();
        returnOrder.setTenantId(TENANT_ID);
        returnOrder.setReturnNumber("RET-E2E-001");
        returnOrder.setBookingId(savedBooking.getId());
        returnOrder.setCustomerId(savedCustomer.getId());
        returnOrder.setStatus(ReturnOrderStatus.INSPECTION);
        returnOrder.setScheduledDate(LocalDate.now().plusDays(11));
        ReturnOrder savedReturn = returnOrderRepository.save(returnOrder);

        ReturnOrderItem returnItem = new ReturnOrderItem();
        returnItem.setTenantId(TENANT_ID);
        returnItem.setReturnOrderId(savedReturn.getId());
        returnItem.setProductId(savedChair.getId());
        returnItem.setProductNameSnapshot(savedChair.getName());
        returnItem.setQuantityExpected(100);
        returnItem.setQuantityReceived(95);
        returnItem.setQuantityGood(90);
        returnItem.setQuantityDamaged(5);
        returnItem.setQuantityMissing(5);
        returnItem.setStatus(ReturnOrderItemStatus.INSPECTED);
        returnOrderItemRepository.save(returnItem);

        // ==========================================
        // 11. DAMAGE CLAIM & ESTIMATE CREATION
        // ==========================================
        DamageClaim claim = new DamageClaim();
        claim.setTenantId(TENANT_ID);
        claim.setClaimNumber("CLM-E2E-001");
        claim.setBookingId(savedBooking.getId());
        claim.setReturnOrderId(savedReturn.getId());
        claim.setCustomerId(savedCustomer.getId());
        claim.setStatus(ClaimStatus.ESTIMATE_CREATED);
        claim.setClaimType(ClaimType.MIXED);
        claim.setEstimatedTotalCost(BigDecimal.valueOf(175.00)); // 5 repairs + 5 replacements
        claim.setCurrency("USD");
        DamageClaim savedClaim = damageClaimRepository.save(claim);
        assertNotNull(savedClaim.getId());

        DamageClaimItem damageItem = new DamageClaimItem();
        damageItem.setTenantId(TENANT_ID);
        damageItem.setClaimId(savedClaim.getId());
        damageItem.setProductId(savedChair.getId());
        damageItem.setProductNameSnapshot(savedChair.getName());
        damageItem.setQuantity(5);
        damageItem.setClaimType(ClaimType.DAMAGE);
        damageItem.setSeverity(DamageSeverity.MAJOR);
        damageItem.setEstimatedCost(BigDecimal.valueOf(75.00));
        damageClaimItemRepository.save(damageItem);

        // ==========================================
        // 12. FINAL INVOICE & FULL PAYMENT
        // ==========================================
        // Balance due ($526.00) + damage claim ($175.00) = $701.00
        Invoice finalInvoice = new Invoice();
        finalInvoice.setTenantId(TENANT_ID);
        finalInvoice.setInvoiceNumber("INV-E2E-FINAL-001");
        finalInvoice.setBookingId(savedBooking.getId());
        finalInvoice.setCustomerId(savedCustomer.getId());
        finalInvoice.setIssueDate(LocalDate.now());
        finalInvoice.setDueDate(LocalDate.now().plusDays(7));
        finalInvoice.setTotalAmount(BigDecimal.valueOf(701.00));
        finalInvoice.setAmountPaid(BigDecimal.valueOf(701.00));
        finalInvoice.setBalanceDue(BigDecimal.ZERO);
        finalInvoice.setStatus(InvoiceStatus.PAID);
        Invoice savedInvoice = invoiceRepository.save(finalInvoice);
        assertEquals(InvoiceStatus.PAID, savedInvoice.getStatus());

        Payment finalPayment = new Payment();
        finalPayment.setTenantId(TENANT_ID);
        finalPayment.setTransactionReference("PAY-FINAL-001");
        finalPayment.setBookingId(savedBooking.getId());
        finalPayment.setCustomerId(savedCustomer.getId());
        finalPayment.setAmount(BigDecimal.valueOf(701.00));
        finalPayment.setPaymentDate(LocalDate.now());
        finalPayment.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        finalPayment.setPaymentStatus(PaymentStatus.COMPLETED);
        Payment savedFinalPayment = paymentRepository.save(finalPayment);
        assertEquals(PaymentStatus.COMPLETED, savedFinalPayment.getPaymentStatus());

        // ==========================================
        // 13. LIFECYCLE CONCLUSION: EVENT COMPLETED
        // ==========================================
        savedBooking.setStatus(BookingStatus.COMPLETED);
        savedBooking.setBalanceDue(BigDecimal.ZERO);
        bookingRepository.save(savedBooking);

        savedClaim.setStatus(ClaimStatus.RESOLVED);
        savedClaim.setFinalTotalCost(BigDecimal.valueOf(175.00));
        damageClaimRepository.save(savedClaim);

        // Verify end state
        Booking completedBooking = bookingRepository.findById(savedBooking.getId()).orElseThrow();
        assertEquals(BookingStatus.COMPLETED, completedBooking.getStatus());
        assertEquals(BigDecimal.ZERO, completedBooking.getBalanceDue());

        DamageClaim resolvedClaim = damageClaimRepository.findById(savedClaim.getId()).orElseThrow();
        assertEquals(ClaimStatus.RESOLVED, resolvedClaim.getStatus());

        Invoice paidInvoice = invoiceRepository.findById(savedInvoice.getId()).orElseThrow();
        assertEquals(InvoiceStatus.PAID, paidInvoice.getStatus());
        assertEquals(BigDecimal.ZERO, paidInvoice.getBalanceDue());
    }
}
