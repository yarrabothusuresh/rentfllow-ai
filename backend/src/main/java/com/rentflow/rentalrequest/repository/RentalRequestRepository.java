package com.rentflow.rentalrequest.repository;

import com.rentflow.rentalrequest.model.RentalRequest;
import com.rentflow.rentalrequest.model.RentalRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RentalRequestRepository extends JpaRepository<RentalRequest, UUID> {

    List<RentalRequest> findByTenantIdOrderByCreatedAtDesc(String tenantId);

    Optional<RentalRequest> findByTenantIdAndId(String tenantId, UUID id);

    Optional<RentalRequest> findByTenantIdAndRequestNumber(String tenantId, String requestNumber);

    Optional<RentalRequest> findByTenantIdAndIdempotencyKey(String tenantId, String idempotencyKey);

    Optional<RentalRequest> findByTenantIdAndConversationId(String tenantId, UUID conversationId);

    List<RentalRequest> findByTenantIdAndStatus(String tenantId, RentalRequestStatus status);
}
