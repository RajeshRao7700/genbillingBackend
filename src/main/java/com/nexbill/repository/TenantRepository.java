package com.nexbill.repository;

import com.nexbill.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {

    Optional<Tenant> findByCode(String code);

    Optional<Tenant> findByEmail(String email);

    boolean existsByCode(String code);

    boolean existsByEmail(String email);
}
