package com.rentflow.payment.repository;

import com.rentflow.payment.model.PaymentAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentAuditRepository extends JpaRepository<PaymentAudit, UUID> {

    List<PaymentAudit> findByTenantIdAndBookingIdOrderByTimestampDesc(String tenantId, UUID bookingId);

    List<PaymentAudit> findByTenantIdAndPaymentIdOrderByTimestampDesc(String tenantId, UUID paymentId);
}
