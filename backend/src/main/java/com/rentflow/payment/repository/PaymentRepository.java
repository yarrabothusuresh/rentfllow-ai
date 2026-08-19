package com.rentflow.payment.repository;

import com.rentflow.payment.model.Payment;
import com.rentflow.payment.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findByTenantIdAndBookingIdOrderByCreatedAtDesc(String tenantId, UUID bookingId);

    List<Payment> findByTenantIdAndBookingIdAndPaymentStatus(String tenantId, UUID bookingId, PaymentStatus paymentStatus);

    Optional<Payment> findByTenantIdAndId(String tenantId, UUID id);
}
