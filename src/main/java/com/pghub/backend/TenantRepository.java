package com.pghub.backend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {
    // We can add custom searches later, like searching for a tenant by their ID number
    boolean existsByGovernmentIdNumber(String governmentIdNumber);
}
