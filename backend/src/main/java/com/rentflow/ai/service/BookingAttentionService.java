package com.rentflow.ai.service;

import com.rentflow.ai.dto.BookingAttentionItemDTO;
import com.rentflow.ai.model.Booking;
import com.rentflow.ai.model.BookingAttentionSignal;
import com.rentflow.ai.model.BookingStatus;
import com.rentflow.ai.repository.BookingRepository;
import com.rentflow.ai.repository.CustomerRepository;
import com.rentflow.delivery.model.Delivery;
import com.rentflow.delivery.model.DeliveryStatus;
import com.rentflow.delivery.repository.DeliveryRepository;
import com.rentflow.invoice.model.Invoice;
import com.rentflow.invoice.model.InvoiceStatus;
import com.rentflow.invoice.repository.InvoiceRepository;
import com.rentflow.warehouse.model.WarehouseOrder;
import com.rentflow.warehouse.model.WarehouseOrderStatus;
import com.rentflow.warehouse.repository.WarehouseOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class BookingAttentionService {

    private final BookingRepository bookingRepository;
    private final WarehouseOrderRepository warehouseOrderRepository;
    private final DeliveryRepository deliveryRepository;
    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;

    public BookingAttentionService(
        BookingRepository bookingRepository,
        WarehouseOrderRepository warehouseOrderRepository,
        DeliveryRepository deliveryRepository,
        InvoiceRepository invoiceRepository,
        CustomerRepository customerRepository
    ) {
        this.bookingRepository = bookingRepository;
        this.warehouseOrderRepository = warehouseOrderRepository;
        this.deliveryRepository = deliveryRepository;
        this.invoiceRepository = invoiceRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional(readOnly = true)
    public List<BookingAttentionItemDTO> getBookingsNeedingAttention(String tenantId, LocalDate startDate, LocalDate endDate) {
        List<Booking> bookings = bookingRepository.findByTenantId(tenantId);
        List<BookingAttentionItemDTO> attentionList = new ArrayList<>();

        LocalDate targetStart = startDate != null ? startDate : LocalDate.now();
        LocalDate targetEnd = endDate != null ? endDate : LocalDate.now().plusDays(7);

        for (Booking b : bookings) {
            // Ignore fully completed or cancelled bookings
            if (b.getStatus() == BookingStatus.COMPLETED || b.getStatus() == BookingStatus.CANCELLED) {
                continue;
            }

            LocalDate bDate = b.getRentalStartDateTime() != null ? b.getRentalStartDateTime().toLocalDate() : null;
            if (bDate != null && (bDate.isBefore(targetStart) || bDate.isAfter(targetEnd))) {
                // If it's outside the target window, only include if severe overdue issue
                if (b.getStatus() != BookingStatus.CONFIRMED && b.getStatus() != BookingStatus.IN_PROGRESS) {
                    continue;
                }
            }

            BookingAttentionItemDTO item = new BookingAttentionItemDTO(
                b.getId(),
                b.getBookingNumber(),
                resolveCustomerName(tenantId, b.getCustomerId()),
                bDate,
                "MEDIUM"
            );

            // Signal 1: Contract signed check
            if (b.getStatus() == BookingStatus.DRAFT || b.getStatus() == BookingStatus.PENDING) {
                item.getSignals().add(BookingAttentionSignal.UNSIGNED_CONTRACT);
                item.getDetails().add("Contract / proposal is not yet executed");
                item.setSeverity("HIGH");
            }

            // Signal 2: Deposit / Financial Check
            List<Invoice> invoices = invoiceRepository.findByTenantIdAndBookingId(tenantId, b.getId());
            boolean depositPaid = false;
            boolean hasOverdue = false;
            for (Invoice inv : invoices) {
                if (inv.getStatus() == InvoiceStatus.OVERDUE || (inv.getDueDate() != null && inv.getDueDate().isBefore(LocalDate.now()) && inv.getStatus() != InvoiceStatus.PAID)) {
                    hasOverdue = true;
                }
                if (inv.getStatus() == InvoiceStatus.PAID) {
                    depositPaid = true;
                }
            }

            if (hasOverdue) {
                item.getSignals().add(BookingAttentionSignal.OVERDUE_INVOICE);
                item.getDetails().add("Invoice payment is overdue");
                item.setSeverity("HIGH");
            } else if (!depositPaid && !invoices.isEmpty() && bDate != null && bDate.isBefore(LocalDate.now().plusDays(3))) {
                item.getSignals().add(BookingAttentionSignal.UNPAID_DEPOSIT);
                item.getDetails().add("Deposit payment not recorded within 3 days of event");
                item.setSeverity("HIGH");
            }

            // Signal 3: Warehouse readiness
            Optional<WarehouseOrder> whOrderOpt = warehouseOrderRepository.findByTenantIdAndBookingId(tenantId, b.getId());
            if (whOrderOpt.isPresent()) {
                WarehouseOrder wo = whOrderOpt.get();
                if (wo.getStatus() == WarehouseOrderStatus.PENDING && bDate != null && !bDate.isAfter(LocalDate.now().plusDays(2))) {
                    item.getSignals().add(BookingAttentionSignal.WAREHOUSE_NOT_READY);
                    item.getDetails().add("Warehouse order " + wo.getOrderNumber() + " not yet picked for upcoming event");
                    item.setSeverity("HIGH");
                }
            }

            // Signal 4: Delivery driver & vehicle assignment
            List<Delivery> deliveries = deliveryRepository.findByTenantId(tenantId).stream()
                .filter(d -> d.getBookingId() != null && d.getBookingId().equals(b.getId()))
                .toList();

            for (Delivery del : deliveries) {
                if (del.getStatus() != DeliveryStatus.DELIVERED && del.getStatus() != DeliveryStatus.CANCELLED) {
                    if (del.getDriverId() == null) {
                        item.getSignals().add(BookingAttentionSignal.DELIVERY_UNASSIGNED);
                        item.getDetails().add("Delivery " + del.getDeliveryNumber() + " has no assigned driver");
                        item.setSeverity("HIGH");
                    }
                    if (del.getVehicleId() == null) {
                        item.getSignals().add(BookingAttentionSignal.VEHICLE_UNASSIGNED);
                        item.getDetails().add("Delivery " + del.getDeliveryNumber() + " has no assigned vehicle");
                    }
                }
            }

            if (!item.getSignals().isEmpty()) {
                attentionList.add(item);
            }
        }

        // Sort by severity (HIGH first) then event date
        attentionList.sort((a, b) -> {
            int sevA = "HIGH".equals(a.getSeverity()) ? 0 : 1;
            int sevB = "HIGH".equals(b.getSeverity()) ? 0 : 1;
            if (sevA != sevB) return Integer.compare(sevA, sevB);
            if (a.getEventDate() != null && b.getEventDate() != null) {
                return a.getEventDate().compareTo(b.getEventDate());
            }
            return 0;
        });

        return attentionList;
    }

    private String resolveCustomerName(String tenantId, UUID customerId) {
        if (customerId == null) return "Unknown Customer";
        return customerRepository.findByTenantIdAndId(tenantId, customerId)
            .map(c -> {
                String name = ((c.getFirstName() != null ? c.getFirstName() : "") + " " + (c.getLastName() != null ? c.getLastName() : "")).trim();
                return !name.isEmpty() ? name : (c.getCompanyName() != null ? c.getCompanyName() : "Customer");
            })
            .orElse("Customer");
    }
}
