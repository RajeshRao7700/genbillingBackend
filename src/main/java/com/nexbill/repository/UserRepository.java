package com.nexbill.repository;

import com.nexbill.entity.User;
import com.nexbill.entity.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE LOWER(u.username) = LOWER(:identifier) OR LOWER(u.email) = LOWER(:identifier)")
    Optional<User> findByUsernameOrEmailIgnoreCase(@Param("identifier") String identifier);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    // Tenant-isolated repository operations
    List<User> findAllByTenantId(Long tenantId);

    Page<User> findAllByTenantId(Long tenantId, Pageable pageable);

    Optional<User> findByIdAndTenantId(Long id, Long tenantId);

    boolean existsByIdAndTenantId(Long id, Long tenantId);

    boolean existsByTenantIdAndUsernameIgnoreCase(Long tenantId, String username);

    boolean existsByTenantIdAndEmailIgnoreCase(Long tenantId, String email);

    // Role-based counts for last-owner protection
    long countByTenantIdAndRoleAndIsActiveTrue(Long tenantId, UserRole role);

    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId AND u.branchId = :branchId")
    List<User> findAllByTenantIdAndBranchId(@Param("tenantId") Long tenantId, @Param("branchId") Long branchId);
}
