package com.rentflow.analytics;

import com.rentflow.ai.model.*;
import com.rentflow.ai.repository.*;
import com.rentflow.analytics.controller.AnalyticsController;
import com.rentflow.analytics.dto.*;
import com.rentflow.analytics.service.*;
import com.rentflow.claims.model.*;
import com.rentflow.claims.repository.DamageClaimRepository;
import com.rentflow.claims.repository.RepairOrderRepository;
import com.rentflow.invoice.model.Invoice;
import com.rentflow.invoice.model.InvoiceStatus;
import com.rentflow.invoice.repository.InvoiceRepository;
import com.rentflow.payment.model.Payment;
import com.rentflow.payment.model.PaymentMethod;
import com.rentflow.payment.model.PaymentStatus;
import com.rentflow.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class AnalyticsServiceTest {

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private RevenueAnalyticsService revenueAnalyticsService;

    @Autowired
    private ProfitabilityService profitabilityService;

    @Autowired
    private InventoryAnalyticsService inventoryAnalyticsService;

    @Autowired
    private CustomerAnalyticsService customerAnalyticsService;

    @Autowired
    private QuoteAnalyticsService quoteAnalyticsService;

    @Autowired
    private OperationsAnalyticsService operationsAnalyticsService;

    @Autowired
    private AnalyticsExportService analyticsExportService;

    @Autowired
    private AnalyticsController analyticsController;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingItemRepository bookingItemRepository;

    @Autowired
    private QuoteRepository quoteRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private DamageClaimRepository damageClaimRepository;

    @Autowired
    private RepairOrderRepository repairOrderRepository;

    private String tenantA = "tenant-a-test";
    private String tenantB = "tenant-b-test";

    private Customer customerA;
    private Product productA;
    private Booking bookingA1;

    @BeforeEach
    public void setup() {
        // Setup Tenant A Customer
        customerA = new Customer();
        customerA.setTenantId(tenantA);
        customerA.setCustomerNumber("CUS-TEST-001");
        customerA.setFirstName("Summit");
        customerA.setLastName("Events");
        customerA.setCompanyName("Summit Events Corp");
        customerA.setEmail("summit@example.com");
        customerA.setCustomerType(CustomerType.CORPORATE);
        customerA = customerRepository.save(customerA);

        // Setup Tenant A Product
        productA = new Product();
        productA.setTenantId(tenantA);
        productA.setSku("TBL-RND-60");
        productA.setName("60-inch Round Table");
        productA.setRentalPrice(new BigDecimal("15.00"));
        productA.setReplacementCost(new BigDecimal("180.00"));
        productA.setQuantityOwned(50);
        productA = productRepository.save(productA);

        // Setup Booking A1
        bookingA1 = new Booking();
        bookingA1.setTenantId(tenantA);
        bookingA1.setBookingNumber("BOOK-TEST-001");
        bookingA1.setQuoteId(UUID.randomUUID());
        bookingA1.setCustomerId(customerA.getId());
        bookingA1.setEventId(UUID.randomUUID());
        bookingA1.setStatus(BookingStatus.CONFIRMED);
        bookingA1.setBookingDate(LocalDate.now());
        bookingA1.setRentalStartDateTime(LocalDateTime.now());
        bookingA1.setRentalEndDateTime(LocalDateTime.now().plusHours(8));
        bookingA1.setSubtotal(new BigDecimal("1500.00"));
        bookingA1.setDeliveryFee(new BigDecimal("100.00"));
        bookingA1.setSetupFee(new BigDecimal("80.00"));
        bookingA1.setTotalAmount(new BigDecimal("1680.00"));
        bookingA1.setDepositPaid(new BigDecimal("1680.00"));
        bookingA1.setBalanceDue(BigDecimal.ZERO);
        bookingA1 = bookingRepository.save(bookingA1);

        BookingItem item1 = new BookingItem();
        item1.setBookingId(bookingA1.getId());
        item1.setProductId(productA.getId());
        item1.setDescription("60-inch Round Table");
        item1.setQuantity(20);
        item1.setUnitPrice(new BigDecimal("15.00"));
        item1.setLineSubtotal(new BigDecimal("300.00"));
        item1.setRentalStartDateTime(bookingA1.getRentalStartDateTime());
        item1.setRentalEndDateTime(bookingA1.getRentalEndDateTime());
        bookingItemRepository.save(item1);

        // Invoice & Payment
        Invoice inv = new Invoice();
        inv.setTenantId(tenantA);
        inv.setBookingId(bookingA1.getId());
        inv.setCustomerId(customerA.getId());
        inv.setInvoiceNumber("INV-TEST-001");
        inv.setIssueDate(LocalDate.now());
        inv.setDueDate(LocalDate.now().plusDays(14));
        inv.setSubtotal(new BigDecimal("1500.00"));
        inv.setFees(new BigDecimal("180.00"));
        inv.setTotalAmount(new BigDecimal("1680.00"));
        inv.setAmountPaid(new BigDecimal("1680.00"));
        inv.setBalanceDue(BigDecimal.ZERO);
        inv.setStatus(InvoiceStatus.PAID);
        invoiceRepository.save(inv);

        Payment pay = new Payment();
        pay.setTenantId(tenantA);
        pay.setBookingId(bookingA1.getId());
        pay.setCustomerId(customerA.getId());
        pay.setAmount(new BigDecimal("1680.00"));
        pay.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        pay.setPaymentStatus(PaymentStatus.COMPLETED);
        pay.setPaymentDate(LocalDate.now());
        paymentRepository.save(pay);
    }

    @Test
    public void testRevenueAnalyticsCalculation() {
        RevenueAnalyticsDTO rev = revenueAnalyticsService.getRevenueAnalytics(tenantA, DateRangeType.THIS_MONTH, null, null);
        assertNotNull(rev);
        assertEquals(new BigDecimal("1680.00"), rev.getTotalBookedRevenue());
        assertEquals(new BigDecimal("1680.00"), rev.getTotalInvoicedRevenue());
        assertEquals(new BigDecimal("1680.00"), rev.getTotalCollectedRevenue());
        assertEquals(BigDecimal.ZERO, rev.getTotalOutstandingRevenue());
        assertEquals(new BigDecimal("1680.00"), rev.getAverageBookingValue());
    }

    @Test
    public void testBookingProfitabilityAndDetail() {
        List<BookingProfitabilityDTO> list = profitabilityService.getBookingProfitabilityList(tenantA, DateRangeType.THIS_MONTH, null, null);
        assertFalse(list.isEmpty());

        BookingProfitabilityDTO bp = list.get(0);
        assertEquals("BOOK-TEST-001", bp.getBookingNumber());
        assertEquals(new BigDecimal("1680.00"), bp.getTotalRevenue());
        assertTrue(bp.getGrossProfit().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(bp.getGrossMarginPercent().compareTo(BigDecimal.ZERO) > 0);

        BookingProfitDetailDTO detail = profitabilityService.getBookingProfitDetail(tenantA, bookingA1.getId());
        assertNotNull(detail);
        assertEquals(new BigDecimal("1500.00"), detail.getRentalRevenue());
        assertEquals(new BigDecimal("100.00"), detail.getDeliveryRevenue());
        assertEquals(new BigDecimal("80.00"), detail.getSetupRevenue());
        assertFalse(detail.getCostSources().isEmpty());
    }

    @Test
    public void testNegativeMarginBookingDetection() {
        Booking lossBooking = new Booking();
        lossBooking.setTenantId(tenantA);
        lossBooking.setBookingNumber("BOOK-LOSS-999");
        lossBooking.setQuoteId(UUID.randomUUID());
        lossBooking.setCustomerId(customerA.getId());
        lossBooking.setEventId(UUID.randomUUID());
        lossBooking.setStatus(BookingStatus.CONFIRMED);
        lossBooking.setBookingDate(LocalDate.now());
        lossBooking.setRentalStartDateTime(LocalDateTime.now().plusDays(1));
        lossBooking.setRentalEndDateTime(LocalDateTime.now().plusDays(2));
        lossBooking.setSubtotal(new BigDecimal("300.00"));
        lossBooking.setDiscountAmount(new BigDecimal("200.00"));
        lossBooking.setTotalAmount(new BigDecimal("100.00")); // $100 revenue vs high cost
        lossBooking.setDepositPaid(new BigDecimal("100.00"));
        lossBooking.setBalanceDue(BigDecimal.ZERO);
        bookingRepository.save(lossBooking);

        List<BookingProfitabilityDTO> list = profitabilityService.getBookingProfitabilityList(tenantA, DateRangeType.THIS_MONTH, null, null);
        BookingProfitabilityDTO lossDto = list.stream().filter(b -> "BOOK-LOSS-999".equals(b.getBookingNumber())).findFirst().orElse(null);
        assertNotNull(lossDto);
        assertEquals("NEGATIVE_MARGIN", lossDto.getMarginFlag());
        assertTrue(lossDto.isWarningFlag());
    }

    @Test
    public void testProductProfitabilityAndUtilization() {
        List<ProductProfitabilityDTO> prods = profitabilityService.getProductProfitabilityList(tenantA, DateRangeType.THIS_MONTH, null, null);
        assertFalse(prods.isEmpty());
        ProductProfitabilityDTO table = prods.stream().filter(p -> "TBL-RND-60".equals(p.getSku())).findFirst().orElse(null);
        assertNotNull(table);
        assertEquals(new BigDecimal("300.00"), table.getRentalRevenue());
        assertEquals(20, table.getTotalQuantityRented());
        assertTrue(table.getProfit().compareTo(BigDecimal.ZERO) > 0);

        InventoryUtilizationDTO util = inventoryAnalyticsService.getInventoryUtilization(tenantA, DateRangeType.THIS_MONTH, null, null);
        assertNotNull(util);
        assertEquals(1, util.getTotalTrackedProducts());
        assertEquals(50, util.getTotalFleetUnits());
    }

    @Test
    public void testCustomerLifetimeAnalytics() {
        List<CustomerAnalyticsDTO> custs = customerAnalyticsService.getCustomerAnalyticsList(tenantA, DateRangeType.THIS_MONTH, null, null);
        assertFalse(custs.isEmpty());
        CustomerAnalyticsDTO ca = custs.stream().filter(c -> "CUS-TEST-001".equals(c.getCustomerNumber())).findFirst().orElse(null);
        assertNotNull(ca);
        assertEquals(new BigDecimal("1680.00"), ca.getLifetimeRevenue());
        assertEquals(new BigDecimal("1680.00"), ca.getLifetimeCollected());
        assertEquals(1, ca.getTotalBookingsCount());
    }

    @Test
    public void testQuoteAnalyticsAndFunnel() {
        Quote q = new Quote();
        q.setTenantId(tenantA);
        q.setQuoteNumber("Q-TEST-001");
        q.setCustomerId(customerA.getId());
        q.setEventId(UUID.randomUUID());
        q.setStatus(QuoteStatus.ACCEPTED);
        q.setQuoteDate(LocalDate.now());
        q.setValidUntil(LocalDate.now().plusDays(7));
        q.setRentalStartDateTime(LocalDateTime.now().plusDays(2));
        q.setRentalEndDateTime(LocalDateTime.now().plusDays(3));
        q.setSubtotal(new BigDecimal("2000.00"));
        q.setTotalAmount(new BigDecimal("2160.00"));
        quoteRepository.save(q);

        QuoteAnalyticsDTO qa = quoteAnalyticsService.getQuoteAnalytics(tenantA, DateRangeType.THIS_MONTH, null, null);
        assertNotNull(qa);
        assertEquals(1, qa.getTotalQuotesCreated());
        assertEquals(1, qa.getQuotesApproved());
        assertEquals(new BigDecimal("100.00"), qa.getOverallConversionRate());
        assertEquals(5, qa.getSalesFunnel().size());
    }

    @Test
    public void testArAgingBuckets() {
        Invoice overdueInv = new Invoice();
        overdueInv.setTenantId(tenantA);
        overdueInv.setBookingId(UUID.randomUUID());
        overdueInv.setCustomerId(customerA.getId());
        overdueInv.setInvoiceNumber("INV-OVERDUE-01");
        overdueInv.setIssueDate(LocalDate.now().minusDays(45));
        overdueInv.setDueDate(LocalDate.now().minusDays(35)); // 35 days past due -> 31-60 bucket
        overdueInv.setSubtotal(new BigDecimal("500.00"));
        overdueInv.setTotalAmount(new BigDecimal("500.00"));
        overdueInv.setAmountPaid(BigDecimal.ZERO);
        overdueInv.setBalanceDue(new BigDecimal("500.00"));
        overdueInv.setStatus(InvoiceStatus.OVERDUE);
        invoiceRepository.save(overdueInv);

        PaymentAndArAnalyticsDTO ar = operationsAnalyticsService.getPaymentAndArAnalytics(tenantA, DateRangeType.THIS_MONTH, null, null);
        assertNotNull(ar);
        assertEquals(new BigDecimal("500.00"), ar.getTotalOverdueAmount());

        ArAgingBucketDTO bucket31to60 = ar.getArAgingBuckets().stream().filter(b -> "31–60 DAYS".equals(b.getBucketName())).findFirst().orElse(null);
        assertNotNull(bucket31to60);
        assertEquals(new BigDecimal("500.00"), bucket31to60.getAmount());
        assertEquals(1, bucket31to60.getInvoiceCount());
    }

    @Test
    public void testTenantIsolation() {
        RevenueAnalyticsDTO revA = revenueAnalyticsService.getRevenueAnalytics(tenantA, DateRangeType.THIS_MONTH, null, null);
        RevenueAnalyticsDTO revB = revenueAnalyticsService.getRevenueAnalytics(tenantB, DateRangeType.THIS_MONTH, null, null);

        assertEquals(new BigDecimal("1680.00"), revA.getTotalBookedRevenue());
        assertEquals(BigDecimal.ZERO, revB.getTotalBookedRevenue());
    }

    @Test
    public void testRbacCustomerAndDriverBlocked() {
        assertThrows(SecurityException.class, () -> {
            analyticsController.getDashboard(tenantA, "CUSTOMER", DateRangeType.THIS_MONTH, null, null);
        });

        assertThrows(SecurityException.class, () -> {
            analyticsController.getDashboard(tenantA, "DRIVER", DateRangeType.THIS_MONTH, null, null);
        });

        // Authorized internal roles succeed
        ResponseEntity<ExecutiveDashboardDTO> respOwner = analyticsController.getDashboard(tenantA, "OWNER", DateRangeType.THIS_MONTH, null, null);
        assertEquals(HttpStatus.OK, respOwner.getStatusCode());

        ResponseEntity<ExecutiveDashboardDTO> respFinance = analyticsController.getDashboard(tenantA, "FINANCE", DateRangeType.THIS_MONTH, null, null);
        assertEquals(HttpStatus.OK, respFinance.getStatusCode());
    }

    @Test
    public void testCsvReportExport() {
        String csvBookings = analyticsExportService.exportReportCsv(tenantA, "bookings", DateRangeType.THIS_MONTH, null, null);
        assertNotNull(csvBookings);
        assertTrue(csvBookings.contains("Booking Number,Customer Name"));
        assertTrue(csvBookings.contains("BOOK-TEST-001"));

        String csvProducts = analyticsExportService.exportReportCsv(tenantA, "products", DateRangeType.THIS_MONTH, null, null);
        assertNotNull(csvProducts);
        assertTrue(csvProducts.contains("SKU,Product Name"));
        assertTrue(csvProducts.contains("TBL-RND-60"));
    }
}
