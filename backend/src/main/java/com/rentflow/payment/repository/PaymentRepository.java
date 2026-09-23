package com.rentflow.payment.repository;

import com.rentflow.payment.model.Payment;
import com.rentflow.payment.model.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findByTenantId(String tenantId);

    Page<Payment> findByTenantId(String tenantId, Pageable pageable);

    List<Payment> findByTenantIdAndBookingIdOrderByCreatedAtDesc(String tenantId, UUID bookingId);

    Page<Payment> findByTenantIdAndBookingId(String tenantId, UUID bookingId, Pageable pageable);

    List<Payment> findByTenantIdAndInvoiceIdOrderByCreatedAtDesc(String tenantId, UUID invoiceId);

    Page<Payment> findByTenantIdAndInvoiceId(String tenantId, UUID invoiceId, Pageable pageable);

    List<Payment> findByTenantIdAndBookingIdAndPaymentStatus(String tenantId, UUID bookingId, PaymentStatus paymentStatus);

    Optional<Payment> findByTenantIdAndId(String tenantId, UUID id);

    Optional<Payment> findByTenantIdAndTransactionReference(String tenantId, String transactionReference);
}
