package com.rentflow.integration.repository;

import com.rentflow.integration.model.DeliveryStatus;
import com.rentflow.integration.model.WebhookDelivery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WebhookDeliveryRepository extends JpaRepository<WebhookDelivery, UUID> {
    List<WebhookDelivery> findByTenantId(String tenantId);
    Page<WebhookDelivery> findByTenantIdOrderByCreatedAtDesc(String tenantId, Pageable pageable);
    List<WebhookDelivery> findByTenantIdAndWebhookEndpointIdOrderByCreatedAtDesc(String tenantId, UUID webhookEndpointId);
    Page<WebhookDelivery> findByTenantIdAndWebhookEndpointIdOrderByCreatedAtDesc(String tenantId, UUID webhookEndpointId, Pageable pageable);
    List<WebhookDelivery> findByTenantIdAndStatus(String tenantId, DeliveryStatus status);
    Page<WebhookDelivery> findByTenantIdAndStatusOrderByCreatedAtDesc(String tenantId, DeliveryStatus status, Pageable pageable);
    Optional<WebhookDelivery> findByTenantIdAndId(String tenantId, UUID id);
    long countByTenantIdAndCreatedAtAfter(String tenantId, LocalDateTime after);
    long countByTenantIdAndStatus(String tenantId, DeliveryStatus status);
}
