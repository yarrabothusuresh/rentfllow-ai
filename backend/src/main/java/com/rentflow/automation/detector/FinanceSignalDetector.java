package com.rentflow.automation.detector;

import com.rentflow.ai.model.Booking;
import com.rentflow.ai.model.BookingStatus;
import com.rentflow.ai.model.Customer;
import com.rentflow.ai.repository.BookingRepository;
import com.rentflow.ai.repository.CustomerRepository;
import com.rentflow.automation.model.AutomationActionType;
import com.rentflow.automation.model.BusinessSignalCategory;
import com.rentflow.automation.model.BusinessSignalSeverity;
import com.rentflow.automation.model.BusinessSignalType;
import com.rentflow.invoice.model.Invoice;
import com.rentflow.invoice.model.InvoiceStatus;
import com.rentflow.invoice.repository.InvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Component
public class FinanceSignalDetector implements BusinessSignalDetector {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Override
    public List<BusinessSignalType> getSupportedTypes() {
        return List.of(
            BusinessSignalType.INVOICE_OVERDUE,
            BusinessSignalType.DEPOSIT_UNPAID,
            BusinessSignalType.CONTRACT_UNSIGNED
        );
    }

    @Override
    public List<DetectedSignal> detect(String tenantId) {
        List<DetectedSignal> signals = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // 1. INVOICE_OVERDUE
        List<Invoice> invoices = invoiceRepository.findByTenantId(tenantId);
        for (Invoice inv : invoices) {
            if (inv.getStatus() == InvoiceStatus.PAID || inv.getStatus() == InvoiceStatus.VOID || inv.getStatus() == InvoiceStatus.DRAFT) {
                continue;
            }

            boolean isOverdue = inv.getStatus() == InvoiceStatus.OVERDUE || 
                                (inv.getDueDate() != null && inv.getDueDate().isBefore(today) && inv.getBalanceDue() != null && inv.getBalanceDue().compareTo(BigDecimal.ZERO) > 0);

            if (isOverdue) {
                Customer customer = inv.getCustomerId() != null ? customerRepository.findById(inv.getCustomerId()).orElse(null) : null;
                String custName = customer != null ? (customer.getFirstName() + " " + customer.getLastName()).trim() : "Customer";
                String custEmail = customer != null ? customer.getEmail() : "";
                long daysOverdue = inv.getDueDate() != null ? ChronoUnit.DAYS.between(inv.getDueDate(), today) : 1;

                Map<String, Object> evidence = new LinkedHashMap<>();
                evidence.put("invoiceNumber", inv.getInvoiceNumber());
                evidence.put("customerName", custName);
                evidence.put("customerEmail", custEmail);
                evidence.put("dueDate", inv.getDueDate() != null ? inv.getDueDate().toString() : "");
                evidence.put("daysOverdue", daysOverdue);
                evidence.put("totalAmount", inv.getTotalAmount() != null ? inv.getTotalAmount().toString() : "0.00");
                evidence.put("balanceDue", inv.getBalanceDue() != null ? inv.getBalanceDue().toString() : "0.00");

                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("invoiceId", inv.getId().toString());
                payload.put("invoiceNumber", inv.getInvoiceNumber());
                payload.put("customerName", custName);
                payload.put("customerEmail", custEmail);
                payload.put("balanceDue", inv.getBalanceDue() != null ? inv.getBalanceDue().toString() : "0.00");
                payload.put("dueDate", inv.getDueDate() != null ? inv.getDueDate().toString() : "");

                signals.add(new DetectedSignal(
                    BusinessSignalType.INVOICE_OVERDUE,
                    BusinessSignalCategory.FINANCE,
                    "INVOICE",
                    inv.getId().toString(),
                    inv.getInvoiceNumber(),
                    daysOverdue > 14 ? BusinessSignalSeverity.CRITICAL : BusinessSignalSeverity.HIGH,
                    tenantId + ":INVOICE_OVERDUE:" + inv.getId(),
                    evidence,
                    AutomationActionType.SEND_INVOICE_REMINDER,
                    payload,
                    "Invoice " + inv.getInvoiceNumber() + " is overdue by " + daysOverdue + " day(s)",
                    "Invoice " + inv.getInvoiceNumber() + " for " + custName + " has an unpaid balance of $" + inv.getBalanceDue() + " (due " + inv.getDueDate() + ").",
                    "Overdue accounts receivables degrade working capital and collection recovery rates."
                ));
            }
        }

        // 2. DEPOSIT_UNPAID & CONTRACT_UNSIGNED from Bookings
        List<Booking> bookings = bookingRepository.findByTenantId(tenantId);
        for (Booking b : bookings) {
            if (b.getStatus() == BookingStatus.CANCELLED || b.getStatus() == BookingStatus.COMPLETED) {
                continue;
            }

            Customer customer = b.getCustomerId() != null ? customerRepository.findById(b.getCustomerId()).orElse(null) : null;
            String custName = customer != null ? (customer.getFirstName() + " " + customer.getLastName()).trim() : "Customer";
            String custEmail = customer != null ? customer.getEmail() : "";

            // Check Deposit
            if (b.getDepositRequired() != null && b.getDepositRequired().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal paid = b.getDepositPaid() != null ? b.getDepositPaid() : BigDecimal.ZERO;
                if (paid.compareTo(b.getDepositRequired()) < 0) {
                    BigDecimal unpaid = b.getDepositRequired().subtract(paid);

                    Map<String, Object> evidence = new LinkedHashMap<>();
                    evidence.put("bookingNumber", b.getBookingNumber());
                    evidence.put("customerName", custName);
                    evidence.put("customerEmail", custEmail);
                    evidence.put("depositRequired", b.getDepositRequired().toString());
                    evidence.put("depositPaid", paid.toString());
                    evidence.put("depositOutstanding", unpaid.toString());
                    evidence.put("rentalStartDate", b.getRentalStartDateTime() != null ? b.getRentalStartDateTime().toLocalDate().toString() : "");

                    Map<String, Object> payload = new LinkedHashMap<>();
                    payload.put("bookingId", b.getId().toString());
                    payload.put("bookingNumber", b.getBookingNumber());
                    payload.put("customerName", custName);
                    payload.put("customerEmail", custEmail);
                    payload.put("depositOutstanding", unpaid.toString());

                    signals.add(new DetectedSignal(
                        BusinessSignalType.DEPOSIT_UNPAID,
                        BusinessSignalCategory.FINANCE,
                        "BOOKING",
                        b.getId().toString(),
                        b.getBookingNumber(),
                        BusinessSignalSeverity.HIGH,
                        tenantId + ":DEPOSIT_UNPAID:" + b.getId(),
                        evidence,
                        AutomationActionType.SEND_DEPOSIT_REMINDER,
                        payload,
                        "Unpaid security deposit for Booking " + b.getBookingNumber(),
                        "Booking " + b.getBookingNumber() + " requires a $" + b.getDepositRequired() + " security deposit, but only $" + paid + " has been received.",
                        "Unpaid deposits leave inventory exposed to unmitigated damage or cancellation risk without collateral."
                    ));
                }
            }

            // Check Contract Unsigned
            if (!Boolean.TRUE.equals(b.getContractSigned())) {
                Map<String, Object> evidence = new LinkedHashMap<>();
                evidence.put("bookingNumber", b.getBookingNumber());
                evidence.put("customerName", custName);
                evidence.put("customerEmail", custEmail);
                evidence.put("contractSigned", false);
                evidence.put("bookingStatus", b.getStatus().name());
                evidence.put("rentalStartDate", b.getRentalStartDateTime() != null ? b.getRentalStartDateTime().toLocalDate().toString() : "");

                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("bookingId", b.getId().toString());
                payload.put("bookingNumber", b.getBookingNumber());
                payload.put("customerName", custName);
                payload.put("customerEmail", custEmail);

                signals.add(new DetectedSignal(
                    BusinessSignalType.CONTRACT_UNSIGNED,
                    BusinessSignalCategory.CONTRACT,
                    "BOOKING",
                    b.getId().toString(),
                    b.getBookingNumber(),
                    BusinessSignalSeverity.HIGH,
                    tenantId + ":CONTRACT_UNSIGNED:" + b.getId(),
                    evidence,
                    AutomationActionType.SEND_CONTRACT_REMINDER,
                    payload,
                    "Rental agreement unsigned for Booking " + b.getBookingNumber(),
                    "Customer " + custName + " has not yet signed the rental contract agreement for booking " + b.getBookingNumber() + ".",
                    "Deploying rental equipment without an executed agreement leaves terms, liability, and indemnification unenforced."
                ));
            }
        }

        return signals;
    }
}
