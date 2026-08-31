package com.rentflow.analytics.service;

import com.rentflow.ai.model.*;
import com.rentflow.ai.repository.*;
import com.rentflow.claims.model.ClaimResolution;
import com.rentflow.claims.model.ClaimStatus;
import com.rentflow.claims.model.ClaimType;
import com.rentflow.claims.model.DamageClaim;
import com.rentflow.claims.model.RepairOrder;
import com.rentflow.claims.model.RepairOrderStatus;
import com.rentflow.claims.repository.DamageClaimRepository;
import com.rentflow.claims.repository.RepairOrderRepository;
import com.rentflow.delivery.model.Delivery;
import com.rentflow.delivery.model.DeliveryStatus;
import com.rentflow.delivery.repository.DeliveryRepository;
import com.rentflow.invoice.model.Invoice;
import com.rentflow.invoice.model.InvoiceStatus;
import com.rentflow.invoice.repository.InvoiceRepository;
import com.rentflow.payment.model.Payment;
import com.rentflow.payment.model.PaymentMethod;
import com.rentflow.payment.model.PaymentStatus;
import com.rentflow.payment.repository.PaymentRepository;
import com.rentflow.returns.model.ReturnOrder;
import com.rentflow.returns.model.ReturnOrderStatus;
import com.rentflow.returns.repository.ReturnOrderRepository;
import com.rentflow.tenant.Tenant;
import com.rentflow.tenant.TenantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Component
@Order(30)
public class AnalyticsDataInitializer implements CommandLineRunner {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductCategoryRepository categoryRepository;

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

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private ReturnOrderRepository returnOrderRepository;

    @Override
    public void run(String... args) throws Exception {
        String tenantId = "tenant-1";

        // 1. Ensure Dance Floor product exists for Underutilized Demo
        Product danceFloor = productRepository.findByTenantId(tenantId).stream()
                .filter(p -> p.getSku() != null && p.getSku().contains("DNC"))
                .findFirst().orElse(null);

        if (danceFloor == null) {
            Product df = new Product();
            df.setTenantId(tenantId);
            df.setSku("DNC-FLR-20");
            df.setName("Oak Parquet Dance Floor 20x20");
            df.setDescription("Modular interlocking wood dance floor package with gold beveled edge trim.");
            df.setRentalPrice(new BigDecimal("650.00"));
            df.setReplacementCost(new BigDecimal("4500.00"));
            df.setQuantityOwned(4);
            productRepository.save(df);
            System.out.println("✅ Seeded Day 24 Dance Floor Product (Underutilized Candidate).");
        }

        // 2. Seed Negative Margin Demo Booking: BOOK-000124
        if (bookingRepository.findByBookingNumber("BOOK-000124").isEmpty()) {
            Customer cust = customerRepository.findByTenantId(tenantId).stream().findFirst().orElse(null);
            UUID custId = cust != null ? cust.getId() : UUID.randomUUID();

            Booking b124 = new Booking();
            b124.setTenantId(tenantId);
            b124.setBookingNumber("BOOK-000124");
            b124.setQuoteId(UUID.randomUUID());
            b124.setCustomerId(custId);
            b124.setEventId(UUID.randomUUID());
            b124.setStatus(BookingStatus.CONFIRMED);
            b124.setBookingDate(LocalDate.now().minusDays(2));
            b124.setRentalStartDateTime(LocalDateTime.now().plusDays(2));
            b124.setRentalEndDateTime(LocalDateTime.now().plusDays(3));
            b124.setSubtotal(new BigDecimal("2600.00"));
            b124.setDiscountAmount(new BigDecimal("600.00")); // heavy discount causing loss
            b124.setDeliveryFee(new BigDecimal("100.00"));
            b124.setSetupFee(new BigDecimal("100.00"));
            b124.setTotalAmount(new BigDecimal("3000.00"));
            b124.setDepositPaid(new BigDecimal("3000.00"));
            b124.setBalanceDue(BigDecimal.ZERO);
            b124.setNotes("Demo Booking with Negative Margin (-11.67% gross margin). Heavy promotional discount and high labor.");
            bookingRepository.save(b124);

            System.out.println("✅ Seeded Day 24 Negative Margin Demo Booking (BOOK-000124).");
        }

        // 3. Seed Realistic Historical Bookings across 6 Months (if < 30 bookings)
        long currentBookingCount = bookingRepository.findByTenantId(tenantId).size();
        if (currentBookingCount < 30) {
            Customer demoCustomer = customerRepository.findByTenantId(tenantId).stream().findFirst().orElse(null);
            UUID cid = demoCustomer != null ? demoCustomer.getId() : UUID.randomUUID();
            Product chair = productRepository.findByTenantId(tenantId).stream().findFirst().orElse(null);
            UUID pid = chair != null ? chair.getId() : UUID.randomUUID();

            LocalDate now = LocalDate.now();
            Random rand = new Random(42);

            for (int i = 1; i <= 60; i++) {
                int monthsAgo = i % 6;
                int dayOffset = (i * 3) % 25 + 1;
                LocalDate bDate = now.minusMonths(monthsAgo).withDayOfMonth(dayOffset);
                String bNum = String.format("BOOK-%06d", 1000 + i);

                if (bookingRepository.findByBookingNumber(bNum).isEmpty()) {
                    Booking b = new Booking();
                    b.setTenantId(tenantId);
                    b.setBookingNumber(bNum);
                    b.setQuoteId(UUID.randomUUID());
                    b.setCustomerId(cid);
                    b.setEventId(UUID.randomUUID());
                    b.setStatus(monthsAgo == 0 ? BookingStatus.CONFIRMED : BookingStatus.COMPLETED);
                    b.setBookingDate(bDate);
                    b.setRentalStartDateTime(bDate.atTime(10, 0));
                    b.setRentalEndDateTime(bDate.plusDays(1).atTime(22, 0));

                    BigDecimal rentSub = BigDecimal.valueOf(600 + rand.nextInt(2200));
                    BigDecimal del = new BigDecimal("120.00");
                    BigDecimal setup = new BigDecimal("80.00");
                    BigDecimal total = rentSub.add(del).add(setup);

                    b.setSubtotal(rentSub);
                    b.setDeliveryFee(del);
                    b.setSetupFee(setup);
                    b.setTotalAmount(total);
                    b.setDepositPaid(total);
                    b.setBalanceDue(BigDecimal.ZERO);
                    bookingRepository.save(b);

                    // Seed Invoice & Payment for this booking
                    Invoice inv = new Invoice();
                    inv.setTenantId(tenantId);
                    inv.setBookingId(b.getId());
                    inv.setCustomerId(cid);
                    inv.setInvoiceNumber("INV-" + bNum);
                    inv.setIssueDate(bDate);
                    inv.setDueDate(bDate.plusDays(14));
                    inv.setSubtotal(rentSub);
                    inv.setFees(del.add(setup));
                    inv.setTotalAmount(total);
                    inv.setAmountPaid(total);
                    inv.setBalanceDue(BigDecimal.ZERO);
                    inv.setStatus(InvoiceStatus.PAID);
                    invoiceRepository.save(inv);

                    Payment p = new Payment();
                    p.setTenantId(tenantId);
                    p.setBookingId(b.getId());
                    p.setCustomerId(cid);
                    p.setAmount(total);
                    p.setPaymentMethod(PaymentMethod.CREDIT_CARD);
                    p.setPaymentStatus(PaymentStatus.COMPLETED);
                    p.setPaymentDate(bDate);
                    paymentRepository.save(p);
                }
            }
            System.out.println("✅ Seeded Day 24 Multi-Month Historical Analytics Data.");
        }
    }
}
