package com.nexbill.repository;

import com.nexbill.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {

    List<Branch> findAllByTenantId(Long tenantId);

    Optional<Branch> findByTenantIdAndIsMainBranchTrue(Long tenantId);

    boolean existsByTenantIdAndBranchCode(Long tenantId, String branchCode);
}
