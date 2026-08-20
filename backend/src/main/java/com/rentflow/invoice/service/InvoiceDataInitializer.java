package com.rentflow.invoice.service;

import com.rentflow.ai.mock.DemoDataRepository;
import com.rentflow.ai.model.Booking;
import com.rentflow.ai.repository.BookingRepository;
import com.rentflow.invoice.dto.InvoiceDTO;
import com.rentflow.invoice.model.Invoice;
import com.rentflow.invoice.model.InvoiceItem;
import com.rentflow.invoice.model.InvoiceStatus;
import com.rentflow.invoice.repository.InvoiceItemRepository;
import com.rentflow.invoice.repository.InvoiceRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
public class InvoiceDataInitializer implements CommandLineRunner {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final InvoiceService invoiceService;
    private final BookingRepository bookingRepository;

    public InvoiceDataInitializer(InvoiceRepository invoiceRepository,
                                  InvoiceItemRepository invoiceItemRepository,
                                  InvoiceService invoiceService,
                                  BookingRepository bookingRepository) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceItemRepository = invoiceItemRepository;
        this.invoiceService = invoiceService;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (invoiceRepository.count() > 0) {
            return;
        }

        String tenantId = DemoDataRepository.EVERGREEN_TENANT_ID;
        List<Booking> bookings = bookingRepository.findByTenantId(tenantId);

        if (bookings.isEmpty()) {
            return;
        }

        // Demo Invoice 1: Emily's Wedding / ABC Events LLC (PARTIALLY_PAID)
        Booking emilyBooking = bookings.get(0);
        try {
            InvoiceDTO inv1 = invoiceService.createInvoiceFromBooking(
                    tenantId,
                    emilyBooking.getId(),
                    "Invoice for Emily's Wedding reception equipment rental.",
                    LocalDate.of(2026, 9, 3),
                    "OWNER"
            );
            System.out.println("✅ Seeded Demo Invoice 1: " + inv1.getInvoiceNumber() + " (Status: " + inv1.getStatus() + ", Balance: $" + inv1.getBalanceDue() + ")");
        } catch (Exception e) {
            System.err.println("⚠️ InvoiceDataInitializer Demo 1 Note: " + e.getMessage());
        }

        // Demo Invoice 2: TechCorp Annual Gala (PAID) - If second booking exists, or seed manually
        if (bookings.size() > 1) {
            Booking techCorpBooking = bookings.get(1);
            try {
                InvoiceDTO inv2 = invoiceService.createInvoiceFromBooking(
                        tenantId,
                        techCorpBooking.getId(),
                        "Invoice for TechCorp Annual Gala.",
                        LocalDate.of(2026, 8, 30),
                        "OWNER"
                );
                System.out.println("✅ Seeded Demo Invoice 2: " + inv2.getInvoiceNumber() + " (Status: " + inv2.getStatus() + ")");
            } catch (Exception e) {
                System.err.println("⚠️ InvoiceDataInitializer Demo 2 Note: " + e.getMessage());
            }
        }

        // Demo Invoice 3: Seed Overdue Demo Invoice manually for demo
        try {
            Invoice inv3 = new Invoice();
            inv3.setTenantId(tenantId);
            inv3.setBookingId(emilyBooking.getId());
            inv3.setCustomerId(emilyBooking.getCustomerId());
            inv3.setInvoiceNumber("INV-000003");
            inv3.setIssueDate(LocalDate.of(2026, 7, 1));
            inv3.setDueDate(LocalDate.of(2026, 7, 15));
            inv3.setSubtotal(new BigDecimal("1200.00"));
            inv3.setDiscount(BigDecimal.ZERO);
            inv3.setFees(BigDecimal.ZERO);
            inv3.setTax(new BigDecimal("99.00"));
            inv3.setTotalAmount(new BigDecimal("1299.00"));
            inv3.setAmountPaid(BigDecimal.ZERO);
            inv3.setBalanceDue(new BigDecimal("1299.00"));
            inv3.setStatus(InvoiceStatus.OVERDUE);
            inv3.setCustomerName("Apex Corporate Events");
            inv3.setCompanyName("Apex Systems Inc.");
            inv3.setEmail("billing@apexsystems.demo");
            inv3.setPhone("(555) 321-9876");
            inv3.setBillingAddress("789 Executive Blvd, Suite 400");
            inv3.setCity("Dallas");
            inv3.setState("TX");
            inv3.setZipCode("75001");
            inv3.setCountry("USA");
            inv3.setNotes("Overdue demo invoice for corporate leadership retreat setup.");
            inv3.setCreatedBy("OWNER");

            Invoice savedInv3 = invoiceRepository.save(inv3);

            InvoiceItem item3 = new InvoiceItem();
            item3.setInvoiceId(savedInv3.getId());
            item3.setDescription("Executive Seminar Stage & Podiums");
            item3.setQuantity(2);
            item3.setUnitPrice(new BigDecimal("600.00"));
            item3.setLineTotal(new BigDecimal("1200.00"));
            invoiceItemRepository.save(item3);

            System.out.println("✅ Seeded Demo Invoice 3: INV-000003 (Status: OVERDUE)");
        } catch (Exception e) {
            System.err.println("⚠️ InvoiceDataInitializer Demo 3 Note: " + e.getMessage());
        }
    }
}
