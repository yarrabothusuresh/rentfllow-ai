package com.rentflow.payment.dto;

import com.rentflow.payment.model.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class RecordPaymentDTO {

    private UUID invoiceId;
    private UUID bookingId;

    @NotNull(message = "Payment amount is required.")
    @DecimalMin(value = "0.01", message = "Payment amount must be greater than zero.")
    private BigDecimal amount;

    @NotNull(message = "Payment method is required.")
    private PaymentMethod paymentMethod;

    private LocalDate paymentDate;

    @NotBlank(message = "Transaction reference is required.")
    @Size(max = 100, message = "Transaction reference must not exceed 100 characters.")
    private String transactionReference;

    @Size(max = 2000, message = "Notes must not exceed 2000 characters.")
    private String notes;

    public RecordPaymentDTO() {}

    public RecordPaymentDTO(BigDecimal amount, PaymentMethod paymentMethod, LocalDate paymentDate, String transactionReference, String notes) {
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentDate = paymentDate;
        this.transactionReference = transactionReference;
        this.notes = notes;
    }

    public RecordPaymentDTO(UUID invoiceId, UUID bookingId, BigDecimal amount, PaymentMethod paymentMethod, LocalDate paymentDate, String transactionReference, String notes) {
        this.invoiceId = invoiceId;
        this.bookingId = bookingId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentDate = paymentDate;
        this.transactionReference = transactionReference;
        this.notes = notes;
    }

    public UUID getInvoiceId() { return invoiceId; }
    public void setInvoiceId(UUID invoiceId) { this.invoiceId = invoiceId; }

    public UUID getBookingId() { return bookingId; }
    public void setBookingId(UUID bookingId) { this.bookingId = bookingId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }

    public String getTransactionReference() { return transactionReference; }
    public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
