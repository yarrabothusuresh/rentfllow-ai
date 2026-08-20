package com.rentflow.invoice.repository;

import com.rentflow.invoice.model.Invoice;
import com.rentflow.invoice.model.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    List<Invoice> findByTenantId(String tenantId);

    Optional<Invoice> findByTenantIdAndId(String tenantId, UUID id);

    Optional<Invoice> findByTenantIdAndBookingId(String tenantId, UUID bookingId);

    List<Invoice> findByTenantIdAndCustomerId(String tenantId, UUID customerId);

    List<Invoice> findByTenantIdAndStatus(String tenantId, InvoiceStatus status);

    boolean existsByTenantIdAndBookingIdAndStatusNot(String tenantId, UUID bookingId, InvoiceStatus status);

    long countByTenantId(String tenantId);

    @Query("SELECT i FROM Invoice i WHERE i.tenantId = :tenantId " +
           "AND (:status IS NULL OR i.status = :status) " +
           "AND (:customerId IS NULL OR i.customerId = :customerId) " +
           "AND (:bookingId IS NULL OR i.bookingId = :bookingId) " +
           "AND (:query IS NULL OR LOWER(i.invoiceNumber) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "     OR LOWER(i.customerName) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "     OR LOWER(i.companyName) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "ORDER BY i.createdAt DESC")
    List<Invoice> searchInvoices(
            @Param("tenantId") String tenantId,
            @Param("status") InvoiceStatus status,
            @Param("customerId") UUID customerId,
            @Param("bookingId") UUID bookingId,
            @Param("query") String query
    );
}
