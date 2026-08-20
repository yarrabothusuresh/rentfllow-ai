package com.rentflow.invoice.service;

import com.rentflow.ai.model.Booking;
import com.rentflow.ai.model.BookingItem;
import com.rentflow.ai.model.Customer;
import com.rentflow.ai.repository.BookingItemRepository;
import com.rentflow.ai.repository.BookingRepository;
import com.rentflow.ai.repository.CustomerRepository;
import com.rentflow.ai.repository.EventRepository;
import com.rentflow.invoice.dto.InvoiceDTO;
import com.rentflow.invoice.dto.InvoiceItemDTO;
import com.rentflow.invoice.model.Invoice;
import com.rentflow.invoice.model.InvoiceAudit;
import com.rentflow.invoice.model.InvoiceItem;
import com.rentflow.invoice.model.InvoiceStatus;
import com.rentflow.invoice.repository.InvoiceAuditRepository;
import com.rentflow.invoice.repository.InvoiceItemRepository;
import com.rentflow.invoice.repository.InvoiceRepository;
import com.rentflow.payment.dto.PaymentDTO;
import com.rentflow.payment.service.PaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final InvoiceAuditRepository invoiceAuditRepository;
    private final BookingRepository bookingRepository;
    private final BookingItemRepository bookingItemRepository;
    private final CustomerRepository customerRepository;
    private final EventRepository eventRepository;
    private final PaymentService paymentService;
    private final TaxService taxService;

    public InvoiceService(InvoiceRepository invoiceRepository,
                          InvoiceItemRepository invoiceItemRepository,
                          InvoiceAuditRepository invoiceAuditRepository,
                          BookingRepository bookingRepository,
                          BookingItemRepository bookingItemRepository,
                          CustomerRepository customerRepository,
                          EventRepository eventRepository,
                          PaymentService paymentService,
                          TaxService taxService) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceItemRepository = invoiceItemRepository;
        this.invoiceAuditRepository = invoiceAuditRepository;
        this.bookingRepository = bookingRepository;
        this.bookingItemRepository = bookingItemRepository;
        this.customerRepository = customerRepository;
        this.eventRepository = eventRepository;
        this.paymentService = paymentService;
        this.taxService = taxService;
    }

    public boolean canManageInvoices(String userRole) {
        if (userRole == null) return false;
        String role = userRole.toUpperCase();
        return "OWNER".equals(role) || "ADMIN".equals(role) || "FINANCE".equals(role);
    }

    public boolean canCreateInvoices(String userRole) {
        if (userRole == null) return false;
        String role = userRole.toUpperCase();
        return "OWNER".equals(role) || "ADMIN".equals(role) || "FINANCE".equals(role) || "SALES".equals(role);
    }

    public synchronized String generateInvoiceNumber(String tenantId) {
        long count = invoiceRepository.countByTenantId(tenantId) + 1;
        return String.format("INV-%06d", count);
    }

    @Transactional
    public InvoiceDTO createInvoiceFromBooking(String tenantId, UUID bookingId, String notes, LocalDate dueDate, String userRole) {
        // 1. RBAC check
        if (!canCreateInvoices(userRole)) {
            throw new SecurityException("You do not have permission to create invoices.");
        }

        // 2. Prevent Duplicate Invoices: Check if an active primary invoice exists
        if (invoiceRepository.existsByTenantIdAndBookingIdAndStatusNot(tenantId, bookingId, InvoiceStatus.VOID)) {
            throw new IllegalStateException("An invoice already exists for this booking.");
        }

        // 3. Load Booking
        Booking booking = bookingRepository.findByTenantIdAndId(tenantId, bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + bookingId));

        // 4. Load Customer
        Customer customer = customerRepository.findById(booking.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with ID: " + booking.getCustomerId()));

        // 5. Load Booking Items
        List<BookingItem> bookingItems = bookingItemRepository.findByBookingId(booking.getId());

        // 6. Calculate & Snapshot Invoice Totals
        BigDecimal subtotal = BigDecimal.ZERO;
        for (BookingItem item : bookingItems) {
            BigDecimal itemTotal = item.getLineSubtotal() != null ? item.getLineSubtotal()
                    : item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotal = subtotal.add(itemTotal);
        }
        if (subtotal.compareTo(BigDecimal.ZERO) == 0 && booking.getSubtotal() != null && booking.getSubtotal().compareTo(BigDecimal.ZERO) > 0) {
            subtotal = booking.getSubtotal();
        } else if (subtotal.compareTo(BigDecimal.ZERO) == 0 && booking.getTotalAmount() != null && booking.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
            subtotal = booking.getTotalAmount();
        }

        BigDecimal discount = booking.getDiscountAmount() != null ? booking.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal fees = BigDecimal.ZERO;
        if (booking.getDeliveryFee() != null) fees = fees.add(booking.getDeliveryFee());
        if (booking.getPickupFee() != null) fees = fees.add(booking.getPickupFee());
        if (booking.getSetupFee() != null) fees = fees.add(booking.getSetupFee());
        if (booking.getBreakdownFee() != null) fees = fees.add(booking.getBreakdownFee());
        if (booking.getServiceFee() != null) fees = fees.add(booking.getServiceFee());

        BigDecimal tax = booking.getTaxAmount() != null && booking.getTaxAmount().compareTo(BigDecimal.ZERO) > 0
                ? booking.getTaxAmount()
                : taxService.calculateTax(subtotal.subtract(discount).add(fees));

        BigDecimal totalAmount = subtotal.subtract(discount).add(fees).add(tax).setScale(2, RoundingMode.HALF_UP);

        // 7. Payments already received
        BigDecimal amountPaid = paymentService.calculateTotalCompletedPaid(tenantId, booking.getId());
        BigDecimal balanceDue = totalAmount.subtract(amountPaid).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);

        // 8. Determine initial status
        InvoiceStatus status = InvoiceStatus.DRAFT;
        if (amountPaid.compareTo(totalAmount) >= 0 && totalAmount.compareTo(BigDecimal.ZERO) > 0) {
            status = InvoiceStatus.PAID;
        } else if (amountPaid.compareTo(BigDecimal.ZERO) > 0) {
            status = InvoiceStatus.PARTIALLY_PAID;
        }

        // 9. Create Invoice Record with Billing Snapshot
        Invoice invoice = new Invoice();
        invoice.setTenantId(tenantId);
        invoice.setBookingId(booking.getId());
        invoice.setCustomerId(customer.getId());
        invoice.setInvoiceNumber(generateInvoiceNumber(tenantId));
        invoice.setIssueDate(LocalDate.now());
        invoice.setDueDate(dueDate != null ? dueDate : LocalDate.now().plusDays(14));
        invoice.setSubtotal(subtotal);
        invoice.setDiscount(discount);
        invoice.setFees(fees);
        invoice.setTax(tax);
        invoice.setTotalAmount(totalAmount);
        invoice.setAmountPaid(amountPaid);
        invoice.setBalanceDue(balanceDue);
        invoice.setStatus(status);
        invoice.setNotes(notes != null ? notes : booking.getNotes());
        invoice.setCreatedBy(userRole != null ? userRole : "System");

        // Customer Snapshot
        invoice.setCustomerName((customer.getFirstName() + " " + (customer.getLastName() != null ? customer.getLastName() : "")).trim());
        invoice.setCompanyName(customer.getCompanyName());
        invoice.setEmail(customer.getEmail());
        invoice.setPhone(customer.getPhone());
        invoice.setBillingAddress(customer.getBillingAddress());
        invoice.setCity(customer.getCity());
        invoice.setState(customer.getState());
        invoice.setZipCode(customer.getZipCode());
        invoice.setCountry(customer.getCountry());

        Invoice savedInvoice = invoiceRepository.save(invoice);

        // 10. Snapshot Line Items
        for (BookingItem bItem : bookingItems) {
            InvoiceItem iItem = new InvoiceItem();
            iItem.setInvoiceId(savedInvoice.getId());
            iItem.setProductId(bItem.getProductId());
            iItem.setDescription(bItem.getDescription());
            iItem.setQuantity(bItem.getQuantity());
            iItem.setUnitPrice(bItem.getUnitPrice());
            iItem.setDiscount(BigDecimal.ZERO);
            iItem.setTax(BigDecimal.ZERO);
            iItem.setLineTotal(bItem.getLineSubtotal() != null ? bItem.getLineSubtotal()
                    : bItem.getUnitPrice().multiply(BigDecimal.valueOf(bItem.getQuantity())));
            invoiceItemRepository.save(iItem);
        }

        // 11. Create Audit Event
        InvoiceAudit audit = new InvoiceAudit(
                tenantId,
                booking.getId(),
                savedInvoice.getId(),
                "INVOICE_CREATED",
                userRole != null ? userRole : "System",
                String.format("Generated Invoice %s for Booking %s (Total: $%s, Paid: $%s, Balance: $%s)",
                        savedInvoice.getInvoiceNumber(),
                        booking.getBookingNumber(),
                        savedInvoice.getTotalAmount(),
                        savedInvoice.getAmountPaid(),
                        savedInvoice.getBalanceDue())
        );
        invoiceAuditRepository.save(audit);

        return mapToDTO(savedInvoice, userRole);
    }

    @Transactional
    public void syncInvoiceWithPayments(String tenantId, UUID bookingId) {
        Optional<Invoice> opt = invoiceRepository.findByTenantIdAndBookingId(tenantId, bookingId);
        if (opt.isEmpty()) return;

        Invoice invoice = opt.get();
        if (invoice.getStatus() == InvoiceStatus.VOID) return;

        BigDecimal amountPaid = paymentService.calculateTotalCompletedPaid(tenantId, bookingId);
        BigDecimal totalAmount = invoice.getTotalAmount();
        BigDecimal balanceDue = totalAmount.subtract(amountPaid).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);

        invoice.setAmountPaid(amountPaid);
        invoice.setBalanceDue(balanceDue);

        InvoiceStatus oldStatus = invoice.getStatus();
        InvoiceStatus newStatus = oldStatus;

        if (balanceDue.compareTo(BigDecimal.ZERO) == 0 && totalAmount.compareTo(BigDecimal.ZERO) > 0) {
            newStatus = InvoiceStatus.PAID;
        } else if (amountPaid.compareTo(BigDecimal.ZERO) > 0) {
            newStatus = InvoiceStatus.PARTIALLY_PAID;
        } else {
            if (LocalDate.now().isAfter(invoice.getDueDate())) {
                newStatus = InvoiceStatus.OVERDUE;
            } else if (oldStatus == InvoiceStatus.PARTIALLY_PAID || oldStatus == InvoiceStatus.PAID) {
                newStatus = InvoiceStatus.SENT;
            }
        }

        invoice.setStatus(newStatus);
        invoiceRepository.save(invoice);

        if (oldStatus != newStatus) {
            InvoiceAudit audit = new InvoiceAudit(
                    tenantId,
                    bookingId,
                    invoice.getId(),
                    "INVOICE_STATUS_CHANGED",
                    "System",
                    String.format("Status automatically updated from %s to %s following payment update. Amount Paid: $%s, Balance Due: $%s",
                            oldStatus, newStatus, amountPaid, balanceDue)
            );
            invoiceAuditRepository.save(audit);
        }
    }

    @Transactional
    public InvoiceDTO updateInvoiceStatus(String tenantId, UUID invoiceId, InvoiceStatus newStatus, String userRole) {
        if (!canManageInvoices(userRole)) {
            throw new SecurityException("You do not have permission to update invoice status.");
        }

        Invoice invoice = invoiceRepository.findByTenantIdAndId(tenantId, invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found with ID: " + invoiceId));

        InvoiceStatus currentStatus = invoice.getStatus();
        if (currentStatus == newStatus) {
            return mapToDTO(invoice, userRole);
        }

        // State Machine Transition Rules
        if (currentStatus == InvoiceStatus.VOID) {
            throw new IllegalStateException("Cannot change status of a voided invoice.");
        }
        if (currentStatus == InvoiceStatus.PAID && (newStatus == InvoiceStatus.DRAFT || newStatus == InvoiceStatus.SENT)) {
            throw new IllegalStateException("Paid invoice cannot be reverted to " + newStatus + ".");
        }

        invoice.setStatus(newStatus);
        Invoice updated = invoiceRepository.save(invoice);

        String action = newStatus == InvoiceStatus.SENT ? "INVOICE_SENT" : "INVOICE_STATUS_CHANGED";
        InvoiceAudit audit = new InvoiceAudit(
                tenantId,
                invoice.getBookingId(),
                invoice.getId(),
                action,
                userRole != null ? userRole : "System",
                String.format("Invoice status updated from %s to %s", currentStatus, newStatus)
        );
        invoiceAuditRepository.save(audit);

        return mapToDTO(updated, userRole);
    }

    @Transactional
    public InvoiceDTO voidInvoice(String tenantId, UUID invoiceId, String reason, String userRole) {
        if (!canManageInvoices(userRole)) {
            throw new SecurityException("You do not have permission to void invoices.");
        }

        Invoice invoice = invoiceRepository.findByTenantIdAndId(tenantId, invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found with ID: " + invoiceId));

        if (invoice.getStatus() == InvoiceStatus.VOID) {
            throw new IllegalStateException("Invoice has already been voided.");
        }

        // Rule: If invoice has associated completed payments, block voiding until payments are handled
        if (invoice.getAmountPaid() != null && invoice.getAmountPaid().compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalStateException(
                    String.format("Invoice cannot be voided because payments totaling $%s are associated with it.",
                            invoice.getAmountPaid().setScale(2, RoundingMode.HALF_UP).toPlainString())
            );
        }

        invoice.setStatus(InvoiceStatus.VOID);
        if (reason != null && !reason.isBlank()) {
            String existingNotes = invoice.getNotes() != null ? invoice.getNotes() + " | " : "";
            invoice.setNotes(existingNotes + "VOID REASON: " + reason);
        }

        Invoice voided = invoiceRepository.save(invoice);

        InvoiceAudit audit = new InvoiceAudit(
                tenantId,
                invoice.getBookingId(),
                invoice.getId(),
                "INVOICE_VOIDED",
                userRole != null ? userRole : "System",
                String.format("Voided Invoice %s. Reason: %s", invoice.getInvoiceNumber(), reason != null ? reason : "N/A")
        );
        invoiceAuditRepository.save(audit);

        return mapToDTO(voided, userRole);
    }

    @Transactional(readOnly = true)
    public Optional<InvoiceDTO> getInvoice(String tenantId, UUID invoiceId, String userRole) {
        return invoiceRepository.findByTenantIdAndId(tenantId, invoiceId)
                .map(i -> mapToDTO(i, userRole));
    }

    @Transactional(readOnly = true)
    public Optional<InvoiceDTO> getInvoiceByBookingId(String tenantId, UUID bookingId, String userRole) {
        return invoiceRepository.findByTenantIdAndBookingId(tenantId, bookingId)
                .map(i -> mapToDTO(i, userRole));
    }

    @Transactional(readOnly = true)
    public List<InvoiceDTO> listInvoices(String tenantId, InvoiceStatus status, UUID customerId, UUID bookingId, String search, String userRole) {
        List<Invoice> invoices = invoiceRepository.searchInvoices(tenantId, status, customerId, bookingId, search != null && !search.isBlank() ? search.trim() : null);

        // Auto-check overdue status on read
        LocalDate today = LocalDate.now();
        List<InvoiceDTO> dtos = new ArrayList<>();
        for (Invoice i : invoices) {
            if ((i.getStatus() == InvoiceStatus.SENT || i.getStatus() == InvoiceStatus.PARTIALLY_PAID)
                    && today.isAfter(i.getDueDate())
                    && i.getBalanceDue().compareTo(BigDecimal.ZERO) > 0) {
                i.setStatus(InvoiceStatus.OVERDUE);
                invoiceRepository.save(i);
            }
            dtos.add(mapToDTO(i, userRole));
        }

        return dtos;
    }

    public InvoiceDTO mapToDTO(Invoice i, String userRole) {
        InvoiceDTO dto = new InvoiceDTO();
        dto.setId(i.getId());
        dto.setTenantId(i.getTenantId());
        dto.setBookingId(i.getBookingId());
        dto.setCustomerId(i.getCustomerId());
        dto.setInvoiceNumber(i.getInvoiceNumber());
        dto.setIssueDate(i.getIssueDate());
        dto.setDueDate(i.getDueDate());
        dto.setSubtotal(i.getSubtotal());
        dto.setDiscount(i.getDiscount());
        dto.setFees(i.getFees());
        dto.setTax(i.getTax());
        dto.setTotalAmount(i.getTotalAmount());
        dto.setAmountPaid(i.getAmountPaid());
        dto.setBalanceDue(i.getBalanceDue());
        dto.setStatus(i.getStatus());
        dto.setNotes(i.getNotes());

        dto.setCustomerName(i.getCustomerName());
        dto.setCompanyName(i.getCompanyName());
        dto.setEmail(i.getEmail());
        dto.setPhone(i.getPhone());
        dto.setBillingAddress(i.getBillingAddress());
        dto.setCity(i.getCity());
        dto.setState(i.getState());
        dto.setZipCode(i.getZipCode());
        dto.setCountry(i.getCountry());

        dto.setCreatedBy(i.getCreatedBy());
        dto.setCreatedAt(i.getCreatedAt());
        dto.setUpdatedAt(i.getUpdatedAt());

        // Booking Number & Event Name mapping
        if (i.getBookingId() != null) {
            bookingRepository.findById(i.getBookingId()).ifPresent(b -> {
                dto.setBookingNumber(b.getBookingNumber());
                if (b.getEventId() != null) {
                    eventRepository.findById(b.getEventId()).ifPresent(e -> dto.setEventName(e.getEventName()));
                }
            });
        }

        // Line Items Mapping
        List<InvoiceItem> items = invoiceItemRepository.findByInvoiceId(i.getId());
        List<InvoiceItemDTO> itemDTOs = new ArrayList<>();
        for (InvoiceItem item : items) {
            InvoiceItemDTO idto = new InvoiceItemDTO();
            idto.setId(item.getId());
            idto.setInvoiceId(item.getInvoiceId());
            idto.setProductId(item.getProductId());
            idto.setDescription(item.getDescription());
            idto.setQuantity(item.getQuantity());
            idto.setUnitPrice(item.getUnitPrice());
            idto.setDiscount(item.getDiscount());
            idto.setTax(item.getTax());
            idto.setLineTotal(item.getLineTotal());
            idto.setCreatedAt(item.getCreatedAt());
            itemDTOs.add(idto);
        }
        dto.setItems(itemDTOs);

        // Associated Payments Mapping
        try {
            List<PaymentDTO> payments = paymentService.getBookingPayments(i.getTenantId(), i.getBookingId());
            dto.setPayments(payments);
        } catch (Exception e) {
            dto.setPayments(Collections.emptyList());
        }

        return dto;
    }
}
