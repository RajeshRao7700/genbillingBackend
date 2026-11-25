package com.nexbill.repository;

import com.nexbill.entity.Branch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {

    List<Branch> findAllByTenantId(Long tenantId);

    Page<Branch> findAllByTenantId(Long tenantId, Pageable pageable);

    Optional<Branch> findByTenantIdAndIsMainBranchTrue(Long tenantId);

    Optional<Branch> findByIdAndTenantId(Long id, Long tenantId);

    boolean existsByTenantIdAndBranchCode(Long tenantId, String branchCode);

    boolean existsByTenantIdAndBranchCodeIgnoreCaseAndIdNot(Long tenantId, String branchCode, Long excludeId);

    boolean existsByTenantIdAndBranchCodeIgnoreCase(Long tenantId, String branchCode);

    long countByTenantIdAndIsMainBranchTrueAndIsActiveTrue(Long tenantId);

    long countByTenantIdAndIsActiveTrue(Long tenantId);
}
