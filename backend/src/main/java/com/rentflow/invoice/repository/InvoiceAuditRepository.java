package com.rentflow.invoice.repository;

import com.rentflow.invoice.model.InvoiceAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InvoiceAuditRepository extends JpaRepository<InvoiceAudit, UUID> {
    List<InvoiceAudit> findByTenantIdAndInvoiceIdOrderByTimestampDesc(String tenantId, UUID invoiceId);
    List<InvoiceAudit> findByTenantIdAndBookingIdOrderByTimestampDesc(String tenantId, UUID bookingId);
}
