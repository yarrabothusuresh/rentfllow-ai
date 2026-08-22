package com.rentflow.user;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    List<User> findByTenant_Id(UUID tenantId);
}
