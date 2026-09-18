package com.rentflow.payment.service;

import com.rentflow.ai.model.Booking;
import com.rentflow.ai.model.BookingStatus;
import com.rentflow.ai.model.Customer;
import com.rentflow.ai.repository.BookingRepository;
import com.rentflow.ai.repository.CustomerRepository;
import com.rentflow.invoice.model.Invoice;
import com.rentflow.invoice.repository.InvoiceRepository;
import com.rentflow.invoice.service.InvoiceService;
import com.rentflow.notification.event.PaymentReceivedEvent;
import com.rentflow.payment.dto.BookingFinancialSummaryDTO;
import com.rentflow.payment.dto.PaymentDTO;
import com.rentflow.payment.dto.RecordPaymentDTO;
import com.rentflow.payment.exception.IdempotencyConflictException;
import com.rentflow.payment.model.Payment;
import com.rentflow.payment.model.PaymentAudit;
import com.rentflow.payment.model.PaymentMethod;
import com.rentflow.payment.model.PaymentStatus;
import com.rentflow.payment.repository.PaymentAuditRepository;
import com.rentflow.payment.repository.PaymentRepository;
import com.rentflow.common.financial.FinancialMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final PaymentAuditRepository paymentAuditRepository;
    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceService invoiceService;
    private final ApplicationEventPublisher eventPublisher;

    public PaymentService(PaymentRepository paymentRepository,
                          PaymentAuditRepository paymentAuditRepository,
                          BookingRepository bookingRepository,
                          CustomerRepository customerRepository,
                          InvoiceRepository invoiceRepository,
                          @Lazy InvoiceService invoiceService,
                          ApplicationEventPublisher eventPublisher) {
        this.paymentRepository = paymentRepository;
        this.paymentAuditRepository = paymentAuditRepository;
        this.bookingRepository = bookingRepository;
        this.customerRepository = customerRepository;
        this.invoiceRepository = invoiceRepository;
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
        return recordPayment(tenantId, bookingId, dto, userRole, null);
    }

    @Transactional
    public PaymentDTO recordPayment(String tenantId, UUID bookingId, RecordPaymentDTO dto, String userRole, UUID customerId) {
        // 1. Validate input dto & amount
        if (dto == null || dto.getAmount() == null) {
            throw new IllegalArgumentException("Payment amount is required.");
        }

        if (dto.getAmount().signum() <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero.");
        }

        if (dto.getPaymentMethod() == null) {
            throw new IllegalArgumentException("Payment method is required.");
        }

        String normalizedRef = (dto.getTransactionReference() != null && !dto.getTransactionReference().isBlank())
                ? dto.getTransactionReference().trim()
                : "TXN-" + UUID.randomUUID().toString();

        // 2. Resolve bookingId and invoiceId
        UUID resolvedBookingId = bookingId != null ? bookingId : dto.getBookingId();
        UUID resolvedInvoiceId = dto.getInvoiceId();

        if (resolvedBookingId == null && resolvedInvoiceId != null) {
            UUID invId = resolvedInvoiceId;
            Invoice invoice = invoiceRepository.findByTenantIdAndId(tenantId, invId)
                    .orElseThrow(() -> new IllegalArgumentException("Invoice not found with ID: " + invId));
            resolvedBookingId = invoice.getBookingId();
        } else if (resolvedBookingId != null && resolvedInvoiceId == null) {
            Optional<Invoice> invOpt = invoiceRepository.findByTenantIdAndBookingId(tenantId, resolvedBookingId);
            if (invOpt.isPresent()) {
                resolvedInvoiceId = invOpt.get().getId();
            }
        }

        if (resolvedBookingId == null) {
            throw new IllegalArgumentException("Either bookingId or invoiceId must be provided.");
        }

        final UUID targetBookingId = resolvedBookingId;
        final UUID targetInvoiceId = resolvedInvoiceId;

        // 3. Authorization check (Staff vs Customer)
        boolean isCustomer = "CUSTOMER".equalsIgnoreCase(userRole);
        if (!isCustomer && !canRecordOrVoidPayment(userRole)) {
            throw new SecurityException("You do not have permission to record payments.");
        }

        // 4. Pre-check: Authoritative tenant-scoped idempotency lookup
        Optional<Payment> existingOpt = paymentRepository.findByTenantIdAndTransactionReference(tenantId, normalizedRef);
        if (existingOpt.isPresent()) {
            Payment existing = existingOpt.get();
            validateReplayMatchesOriginalRequest(existing, dto, targetBookingId, targetInvoiceId);
            log.info("Idempotent payment replay detected: tenantId={}, paymentId={}, transactionReference={}",
                    tenantId, existing.getId(), normalizedRef);
            PaymentDTO replayDto = mapToDTO(existing);
            replayDto.setIdempotentReplay(true);
            return replayDto;
        }

        // 5. Deterministic row-level locking: Lock Booking first, then Invoice
        Booking booking = bookingRepository.findByIdAndTenantIdForUpdate(tenantId, targetBookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + targetBookingId));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Cannot record payment against a cancelled booking.");
        }

        // Customer IDOR validation
        if (isCustomer) {
            if (customerId == null || !customerId.equals(booking.getCustomerId())) {
                throw new SecurityException("Customer IDOR protection: cannot record payment for another customer's booking or invoice.");
            }
        }

        Optional<Invoice> lockedInvoiceOpt = invoiceRepository.findByTenantIdAndBookingIdForUpdate(tenantId, targetBookingId);
        if (targetInvoiceId != null && lockedInvoiceOpt.isPresent()) {
            if (!lockedInvoiceOpt.get().getId().equals(targetInvoiceId)) {
                throw new IllegalArgumentException("Invoice ID mismatch for booking: " + targetBookingId);
            }
        }

        // 5b. Re-check idempotency under lock: A concurrent thread may have committed while this thread waited for the lock
        existingOpt = paymentRepository.findByTenantIdAndTransactionReference(tenantId, normalizedRef);
        if (existingOpt.isPresent()) {
            Payment existing = existingOpt.get();
            validateReplayMatchesOriginalRequest(existing, dto, targetBookingId, targetInvoiceId);
            log.info("Idempotent payment replay detected under lock: tenantId={}, paymentId={}, transactionReference={}",
                    tenantId, existing.getId(), normalizedRef);
            PaymentDTO replayDto = mapToDTO(existing);
            replayDto.setIdempotentReplay(true);
            return replayDto;
        }

        // 6. Calculate existing completed payments and outstanding balance using locked state
        BigDecimal currentPaid = calculateTotalCompletedPaid(tenantId, targetBookingId);
        BigDecimal bookingTotal = booking.getTotalAmount() != null ? booking.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal outstandingBalance = bookingTotal.subtract(currentPaid).max(BigDecimal.ZERO);

        // 7. Overpayment check
        BigDecimal paymentAmount = dto.getAmount().setScale(2, RoundingMode.HALF_UP);
        if (paymentAmount.compareTo(outstandingBalance) > 0) {
            throw new IllegalArgumentException(
                    String.format("Payment exceeds outstanding balance of $%s.",
                            outstandingBalance.setScale(2, RoundingMode.HALF_UP).toPlainString())
            );
        }

        // 8. Create & save Payment record within transaction boundary
        Payment payment = new Payment();
        payment.setTenantId(tenantId);
        payment.setBookingId(targetBookingId);
        payment.setInvoiceId(targetInvoiceId != null ? targetInvoiceId : lockedInvoiceOpt.map(Invoice::getId).orElse(null));
        payment.setCustomerId(booking.getCustomerId());
        payment.setAmount(paymentAmount);
        payment.setPaymentMethod(dto.getPaymentMethod());
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        payment.setPaymentDate(dto.getPaymentDate() != null ? dto.getPaymentDate() : LocalDate.now());
        payment.setTransactionReference(normalizedRef);
        payment.setNotes(dto.getNotes());
        payment.setCreatedBy(userRole != null ? userRole : "System");

        Payment savedPayment;
        try {
            savedPayment = paymentRepository.saveAndFlush(payment);
        } catch (DataIntegrityViolationException ex) {
            // Concurrency race: Another thread committed the exact same (tenantId, transactionReference)
            log.warn("Database unique constraint conflict caught for reference '{}', re-querying existing payment.", normalizedRef);
            Optional<Payment> concurrentDuplicate = paymentRepository.findByTenantIdAndTransactionReference(tenantId, normalizedRef);
            if (concurrentDuplicate.isPresent()) {
                Payment existing = concurrentDuplicate.get();
                validateReplayMatchesOriginalRequest(existing, dto, targetBookingId, targetInvoiceId);
                log.info("Idempotent payment replay detected after concurrent race: tenantId={}, paymentId={}, transactionReference={}",
                        tenantId, existing.getId(), normalizedRef);
                PaymentDTO replayDto = mapToDTO(existing);
                replayDto.setIdempotentReplay(true);
                return replayDto;
            }
            throw ex;
        }

        // 9. Update Booking financial metrics and status atomically
        BigDecimal newPaidSum = currentPaid.add(savedPayment.getAmount());
        updateBookingFinancials(booking, newPaidSum, bookingTotal);

        // 10. Sync associated invoice if present
        invoiceService.syncInvoiceWithPayments(tenantId, targetBookingId);

        // 11. Record audit log
        PaymentAudit audit = new PaymentAudit(
                tenantId,
                targetBookingId,
                savedPayment.getId(),
                "PAYMENT_RECORDED",
                userRole != null ? userRole : "System",
                String.format("Recorded %s payment of $%s (Ref: %s)",
                        savedPayment.getPaymentMethod(),
                        savedPayment.getAmount().setScale(2, RoundingMode.HALF_UP),
                        savedPayment.getTransactionReference())
        );
        paymentAuditRepository.save(audit);

        // 12. Publish event for notification/analytics
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
                targetBookingId,
                booking.getBookingNumber(),
                booking.getCustomerId(),
                customerName,
                savedPayment.getAmount(),
                booking.getBalanceDue()
        ));

        log.info("Payment recorded successfully: tenantId={}, paymentId={}, invoiceId={}, amount={}, transactionReference={}",
                tenantId, savedPayment.getId(), savedPayment.getInvoiceId(), savedPayment.getAmount(), savedPayment.getTransactionReference());

        PaymentDTO result = mapToDTO(savedPayment);
        result.setIdempotentReplay(false);
        return result;
    }

    private void validateReplayMatchesOriginalRequest(Payment existing, RecordPaymentDTO request, UUID targetBookingId, UUID targetInvoiceId) {
        BigDecimal requestedAmount = request.getAmount().setScale(2, RoundingMode.HALF_UP);
        BigDecimal existingAmount = existing.getAmount().setScale(2, RoundingMode.HALF_UP);

        if (existingAmount.compareTo(requestedAmount) != 0) {
            throw new IdempotencyConflictException(
                    String.format("Payment reference '%s' has already been used with a different amount ($%s vs requested $%s).",
                            existing.getTransactionReference(),
                            existingAmount.toPlainString(),
                            requestedAmount.toPlainString())
            );
        }

        if (targetBookingId != null && !existing.getBookingId().equals(targetBookingId)) {
            throw new IdempotencyConflictException(
                    String.format("Payment reference '%s' has already been used for a different booking.",
                            existing.getTransactionReference())
            );
        }

        if (targetInvoiceId != null && existing.getInvoiceId() != null && !existing.getInvoiceId().equals(targetInvoiceId)) {
            throw new IdempotencyConflictException(
                    String.format("Payment reference '%s' has already been used for a different invoice.",
                            existing.getTransactionReference())
            );
        }

        if (request.getPaymentMethod() != null && existing.getPaymentMethod() != request.getPaymentMethod()) {
            throw new IdempotencyConflictException(
                    String.format("Payment reference '%s' has already been used with a different payment method (%s vs %s).",
                            existing.getTransactionReference(), existing.getPaymentMethod(), request.getPaymentMethod())
            );
        }
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

        // 4. Recalculate booking financials with row lock
        Booking booking = bookingRepository.findByIdAndTenantIdForUpdate(tenantId, payment.getBookingId())
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

    @Transactional
    public PaymentDTO refundPayment(String tenantId, UUID paymentId, BigDecimal refundAmount, String userRole, String reason) {
        // 1. Check RBAC
        if (!canRecordOrVoidPayment(userRole)) {
            throw new SecurityException("You do not have permission to refund payments.");
        }

        // 2. Fetch payment
        Payment payment = paymentRepository.findByTenantIdAndId(tenantId, paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found with ID: " + paymentId));

        if (payment.getPaymentStatus() != PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Only COMPLETED payments can be refunded. Current status: " + payment.getPaymentStatus());
        }

        BigDecimal originalAmount = FinancialMath.scaleCurrency(payment.getAmount());
        BigDecimal actualRefund = refundAmount != null ? FinancialMath.scaleCurrency(refundAmount) : originalAmount;

        if (actualRefund.signum() <= 0) {
            throw new IllegalArgumentException("Refund amount must be greater than zero.");
        }

        if (actualRefund.compareTo(originalAmount) > 0) {
            throw new IllegalArgumentException(String.format("Refund amount ($%s) cannot exceed original payment amount ($%s).",
                    actualRefund.toPlainString(), originalAmount.toPlainString()));
        }

        // 3. Mark payment status / update amounts
        boolean isFullRefund = actualRefund.compareTo(originalAmount) == 0;
        if (isFullRefund) {
            payment.setPaymentStatus(PaymentStatus.REFUNDED);
        } else {
            // Partial refund: adjust remaining completed amount
            payment.setAmount(originalAmount.subtract(actualRefund).setScale(FinancialMath.CURRENCY_SCALE, FinancialMath.ROUNDING_MODE));
        }

        String existingNotes = payment.getNotes() != null ? payment.getNotes() + " | " : "";
        payment.setNotes(existingNotes + String.format("REFUNDED: $%s (Reason: %s)", actualRefund, reason != null ? reason : "Not specified"));
        Payment updatedPayment = paymentRepository.save(payment);

        // 4. Lock booking and invoice in strict order to update balances
        Booking booking = bookingRepository.findByIdAndTenantIdForUpdate(tenantId, payment.getBookingId())
                .orElseThrow(() -> new IllegalArgumentException("Booking not found for payment: " + payment.getBookingId()));

        BigDecimal remainingPaidSum = calculateTotalCompletedPaid(tenantId, booking.getId());
        BigDecimal bookingTotal = FinancialMath.scaleCurrency(booking.getTotalAmount());
        updateBookingFinancials(booking, remainingPaidSum, bookingTotal);

        // 5. Sync associated invoice if present
        invoiceService.syncInvoiceWithPayments(tenantId, booking.getId());

        // 6. Record audit log
        PaymentAudit audit = new PaymentAudit(
                tenantId,
                booking.getId(),
                updatedPayment.getId(),
                "PAYMENT_REFUNDED",
                userRole != null ? userRole : "System",
                String.format("Refunded $%s from %s payment (Orig: $%s). Reason: %s",
                        actualRefund, updatedPayment.getPaymentMethod(), originalAmount, reason != null ? reason : "No reason provided")
        );
        paymentAuditRepository.save(audit);

        log.info("Payment refunded successfully: tenantId={}, paymentId={}, refundAmount={}, isFullRefund={}",
                tenantId, paymentId, actualRefund, isFullRefund);

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
    public List<PaymentDTO> getInvoicePayments(String tenantId, UUID invoiceId) {
        return paymentRepository.findByTenantIdAndInvoiceIdOrderByCreatedAtDesc(tenantId, invoiceId)
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

        BigDecimal bookingTotal = FinancialMath.scaleCurrency(booking.getTotalAmount());
        BigDecimal depositRequired = FinancialMath.scaleCurrency(booking.getDepositRequired());
        BigDecimal amountPaid = calculateTotalCompletedPaid(tenantId, bookingId);
        BigDecimal outstandingBalance = bookingTotal.subtract(amountPaid).max(BigDecimal.ZERO)
                .setScale(FinancialMath.CURRENCY_SCALE, FinancialMath.ROUNDING_MODE);

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
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(FinancialMath.CURRENCY_SCALE, FinancialMath.ROUNDING_MODE);
    }

    private void updateBookingFinancials(Booking booking, BigDecimal newPaidSum, BigDecimal bookingTotal) {
        BigDecimal scaledPaid = FinancialMath.scaleCurrency(newPaidSum);
        BigDecimal scaledTotal = FinancialMath.scaleCurrency(bookingTotal);
        booking.setDepositPaid(scaledPaid);
        BigDecimal balanceDue = scaledTotal.subtract(scaledPaid).max(BigDecimal.ZERO)
                .setScale(FinancialMath.CURRENCY_SCALE, FinancialMath.ROUNDING_MODE);
        booking.setBalanceDue(balanceDue);

        // Update status if active booking
        if (booking.getStatus() != BookingStatus.CANCELLED) {
            if (scaledPaid.compareTo(BigDecimal.ZERO) == 0) {
                booking.setStatus(BookingStatus.DEPOSIT_PENDING);
            } else if (scaledPaid.compareTo(scaledTotal) >= 0) {
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
        dto.setInvoiceId(p.getInvoiceId());
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
