package com.rentflow.delivery.repository;

import com.rentflow.delivery.model.DeliveryRouteStop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DeliveryRouteStopRepository extends JpaRepository<DeliveryRouteStop, UUID> {
    List<DeliveryRouteStop> findByRouteIdOrderBySequenceNumberAsc(UUID routeId);
    void deleteByRouteId(UUID routeId);
}
