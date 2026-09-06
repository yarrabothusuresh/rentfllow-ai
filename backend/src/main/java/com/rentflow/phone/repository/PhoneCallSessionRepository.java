package com.rentflow.phone.repository;

import com.rentflow.phone.model.CallStatus;
import com.rentflow.phone.model.PhoneCallSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PhoneCallSessionRepository extends JpaRepository<PhoneCallSession, UUID> {

    Optional<PhoneCallSession> findByIdAndTenantId(UUID id, String tenantId);

    Optional<PhoneCallSession> findByTenantIdAndProviderCallId(String tenantId, String providerCallId);

    Page<PhoneCallSession> findByTenantIdOrderByStartedAtDesc(String tenantId, Pageable pageable);

    Page<PhoneCallSession> findByTenantIdAndStatusOrderByStartedAtDesc(String tenantId, CallStatus status, Pageable pageable);

    long countByTenantId(String tenantId);

    long countByTenantIdAndStatus(String tenantId, CallStatus status);

    long countByTenantIdAndStartedAtAfter(String tenantId, Instant after);

    long countByTenantIdAndLeadIdIsNotNull(String tenantId);
}
