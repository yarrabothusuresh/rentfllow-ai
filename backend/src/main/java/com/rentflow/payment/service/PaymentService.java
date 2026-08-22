package com.rentflow.payment.service;

import com.rentflow.ai.model.Booking;
import com.rentflow.ai.model.BookingStatus;
import com.rentflow.ai.repository.BookingRepository;
import com.rentflow.payment.dto.BookingFinancialSummaryDTO;
import com.rentflow.payment.dto.PaymentDTO;
import com.rentflow.payment.dto.RecordPaymentDTO;
import com.rentflow.payment.model.Payment;
import com.rentflow.payment.model.PaymentAudit;
import com.rentflow.payment.model.PaymentMethod;
import com.rentflow.payment.model.PaymentStatus;
import com.rentflow.payment.repository.PaymentAuditRepository;
import com.rentflow.payment.repository.PaymentRepository;
import com.rentflow.invoice.service.InvoiceService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.rentflow.ai.repository.CustomerRepository;
import com.rentflow.ai.model.Customer;
import com.rentflow.notification.event.PaymentReceivedEvent;
import org.springframework.context.ApplicationEventPublisher;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentAuditRepository paymentAuditRepository;
    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;
    private final InvoiceService invoiceService;
    private final ApplicationEventPublisher eventPublisher;

    public PaymentService(PaymentRepository paymentRepository,
                          PaymentAuditRepository paymentAuditRepository,
                          BookingRepository bookingRepository,
                          CustomerRepository customerRepository,
                          @Lazy InvoiceService invoiceService,
                          ApplicationEventPublisher eventPublisher) {
        this.paymentRepository = paymentRepository;
        this.paymentAuditRepository = paymentAuditRepository;
        this.bookingRepository = bookingRepository;
        this.customerRepository = customerRepository;
        this.invoiceService = invoiceService;
        this.eventPublisher = eventPublisher;
    }

    public boolean canRecordOrVoidPayment(String userRole) {
        if (userRole == null) return false;
        String role = userRole.toUpperCase();
        return "OWNER".equals(role) || "ADMIN".equals(role) || "FINANCE".equals(role);
    }

    @Transactional
    public PaymentDTO recordPayment(String tenantId, UUID bookingId, RecordPaymentDTO dto, String userRole) {
        // 1. Check RBAC
        if (!canRecordOrVoidPayment(userRole)) {
            throw new SecurityException("You do not have permission to record payments.");
        }

        // 2. Validate input dto & amount
        if (dto == null || dto.getAmount() == null) {
            throw new IllegalArgumentException("Payment amount is required.");
        }

        if (dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero.");
        }

        if (dto.getPaymentMethod() == null) {
            throw new IllegalArgumentException("Payment method is required.");
        }

        // 3. Verify booking exists and belongs to tenant
        Booking booking = bookingRepository.findByTenantIdAndId(tenantId, bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + bookingId));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Cannot record payment against a cancelled booking.");
        }

        // 4. Calculate existing completed payments and outstanding balance
        BigDecimal currentPaid = calculateTotalCompletedPaid(tenantId, bookingId);
        BigDecimal bookingTotal = booking.getTotalAmount() != null ? booking.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal outstandingBalance = bookingTotal.subtract(currentPaid).max(BigDecimal.ZERO);

        // 5. Overpayment check
        if (dto.getAmount().compareTo(outstandingBalance) > 0) {
            throw new IllegalArgumentException(
                    String.format("Payment exceeds outstanding balance of $%s.",
                            outstandingBalance.setScale(2, RoundingMode.HALF_UP).toPlainString())
            );
        }

        // 6. Create & save Payment record
        Payment payment = new Payment();
        payment.setTenantId(tenantId);
        payment.setBookingId(bookingId);
        payment.setCustomerId(booking.getCustomerId());
        payment.setAmount(dto.getAmount());
        payment.setPaymentMethod(dto.getPaymentMethod());
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        payment.setPaymentDate(dto.getPaymentDate() != null ? dto.getPaymentDate() : LocalDate.now());
        payment.setTransactionReference(dto.getTransactionReference());
        payment.setNotes(dto.getNotes());
        payment.setCreatedBy(userRole != null ? userRole : "System");

        Payment savedPayment = paymentRepository.save(payment);

        // 7. Update Booking financial metrics and status
        BigDecimal newPaidSum = currentPaid.add(savedPayment.getAmount());
        updateBookingFinancials(booking, newPaidSum, bookingTotal);

        // 8. Sync associated invoice if present
        invoiceService.syncInvoiceWithPayments(tenantId, bookingId);

        // 9. Record audit log
        PaymentAudit audit = new PaymentAudit(
                tenantId,
                bookingId,
                savedPayment.getId(),
                "PAYMENT_RECORDED",
                userRole != null ? userRole : "System",
                String.format("Recorded %s payment of $%s (Ref: %s)",
                        savedPayment.getPaymentMethod(),
                        savedPayment.getAmount().setScale(2, RoundingMode.HALF_UP),
                        savedPayment.getTransactionReference() != null ? savedPayment.getTransactionReference() : "N/A")
        );
        paymentAuditRepository.save(audit);

        String customerName = "Valued Customer";
        if (booking.getCustomerId() != null) {
            Optional<Customer> c = customerRepository.findById(booking.getCustomerId());
            if (c.isPresent()) {
                Customer cust = c.get();
                customerName = cust.getCompanyName() != null && !cust.getCompanyName().isBlank() ? cust.getCompanyName() : cust.getFirstName();
            }
        }
        eventPublisher.publishEvent(new PaymentReceivedEvent(
                tenantId,
                savedPayment.getId(),
                bookingId,
                booking.getBookingNumber(),
                booking.getCustomerId(),
                customerName,
                savedPayment.getAmount(),
                booking.getBalanceDue()
        ));

        return mapToDTO(savedPayment);
    }

    @Transactional
    public PaymentDTO voidPayment(String tenantId, UUID paymentId, String userRole, String reason) {
        // 1. Check RBAC
        if (!canRecordOrVoidPayment(userRole)) {
            throw new SecurityException("You do not have permission to void payments.");
        }

        // 2. Fetch payment
        Payment payment = paymentRepository.findByTenantIdAndId(tenantId, paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found with ID: " + paymentId));

        if (payment.getPaymentStatus() == PaymentStatus.VOID) {
            throw new IllegalStateException("Payment has already been voided.");
        }

        // 3. Mark payment status as VOID
        payment.setPaymentStatus(PaymentStatus.VOID);
        if (reason != null && !reason.isBlank()) {
            String existingNotes = payment.getNotes() != null ? payment.getNotes() + " | " : "";
            payment.setNotes(existingNotes + "VOID REASON: " + reason);
        }
        Payment updatedPayment = paymentRepository.save(payment);

        // 4. Recalculate booking financials
        Booking booking = bookingRepository.findByTenantIdAndId(tenantId, payment.getBookingId())
                .orElseThrow(() -> new IllegalArgumentException("Booking not found for payment: " + payment.getBookingId()));

        BigDecimal remainingPaidSum = calculateTotalCompletedPaid(tenantId, booking.getId());
        BigDecimal bookingTotal = booking.getTotalAmount() != null ? booking.getTotalAmount() : BigDecimal.ZERO;
        updateBookingFinancials(booking, remainingPaidSum, bookingTotal);

        // 5. Sync associated invoice if present
        invoiceService.syncInvoiceWithPayments(tenantId, booking.getId());

        // 6. Record audit log
        PaymentAudit audit = new PaymentAudit(
                tenantId,
                booking.getId(),
                updatedPayment.getId(),
                "PAYMENT_VOIDED",
                userRole != null ? userRole : "System",
                String.format("Voided %s payment of $%s. Reason: %s",
                        updatedPayment.getPaymentMethod(),
                        updatedPayment.getAmount().setScale(2, RoundingMode.HALF_UP),
                        reason != null ? reason : "No reason provided")
        );
        paymentAuditRepository.save(audit);

        return mapToDTO(updatedPayment);
    }

    @Transactional(readOnly = true)
    public List<PaymentDTO> getBookingPayments(String tenantId, UUID bookingId) {
        // Verify booking existence & tenant matching
        bookingRepository.findByTenantIdAndId(tenantId, bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + bookingId));

        return paymentRepository.findByTenantIdAndBookingIdOrderByCreatedAtDesc(tenantId, bookingId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<PaymentDTO> getPayment(String tenantId, UUID paymentId) {
        return paymentRepository.findByTenantIdAndId(tenantId, paymentId)
                .map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public BookingFinancialSummaryDTO getFinancialSummary(String tenantId, UUID bookingId) {
        Booking booking = bookingRepository.findByTenantIdAndId(tenantId, bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + bookingId));

        BigDecimal bookingTotal = booking.getTotalAmount() != null ? booking.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal depositRequired = booking.getDepositRequired() != null ? booking.getDepositRequired() : BigDecimal.ZERO;
        BigDecimal amountPaid = calculateTotalCompletedPaid(tenantId, bookingId);
        BigDecimal outstandingBalance = bookingTotal.subtract(amountPaid).max(BigDecimal.ZERO);

        String statusStr = deriveFinancialStatus(amountPaid, bookingTotal, booking.getStatus());

        return new BookingFinancialSummaryDTO(
                bookingId,
                bookingTotal,
                depositRequired,
                amountPaid,
                outstandingBalance,
                statusStr
        );
    }

    public BigDecimal calculateTotalCompletedPaid(String tenantId, UUID bookingId) {
        List<Payment> completedPayments = paymentRepository.findByTenantIdAndBookingIdAndPaymentStatus(
                tenantId, bookingId, PaymentStatus.COMPLETED);

        return completedPayments.stream()
                .map(Payment::getAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void updateBookingFinancials(Booking booking, BigDecimal newPaidSum, BigDecimal bookingTotal) {
        booking.setDepositPaid(newPaidSum);
        BigDecimal balanceDue = bookingTotal.subtract(newPaidSum).max(BigDecimal.ZERO);
        booking.setBalanceDue(balanceDue);

        // Update status if active booking
        if (booking.getStatus() != BookingStatus.CANCELLED) {
            if (newPaidSum.compareTo(BigDecimal.ZERO) == 0) {
                booking.setStatus(BookingStatus.DEPOSIT_PENDING);
            } else if (newPaidSum.compareTo(bookingTotal) >= 0) {
                booking.setStatus(BookingStatus.PAID);
            } else {
                booking.setStatus(BookingStatus.PARTIALLY_PAID);
            }
        }
        bookingRepository.save(booking);
    }

    public String deriveFinancialStatus(BigDecimal amountPaid, BigDecimal bookingTotal, BookingStatus bookingStatus) {
        if (bookingStatus == BookingStatus.CANCELLED) {
            return "CANCELLED";
        }
        if (amountPaid.compareTo(BigDecimal.ZERO) == 0) {
            return "DEPOSIT_PENDING";
        } else if (amountPaid.compareTo(bookingTotal) >= 0) {
            return "PAID";
        } else {
            return "PARTIALLY_PAID";
        }
    }

    public PaymentDTO mapToDTO(Payment p) {
        PaymentDTO dto = new PaymentDTO();
        dto.setId(p.getId());
        dto.setTenantId(p.getTenantId());
        dto.setBookingId(p.getBookingId());
        dto.setCustomerId(p.getCustomerId());
        dto.setAmount(p.getAmount());
        dto.setPaymentMethod(p.getPaymentMethod());
        dto.setPaymentStatus(p.getPaymentStatus());
        dto.setPaymentDate(p.getPaymentDate());
        dto.setTransactionReference(p.getTransactionReference());
        dto.setNotes(p.getNotes());
        dto.setCreatedBy(p.getCreatedBy());
        dto.setCreatedAt(p.getCreatedAt());
        dto.setUpdatedAt(p.getUpdatedAt());
        return dto;
    }
}
