package com.nexbill.multitenancy;

import com.nexbill.entity.Tenant;
import com.nexbill.exception.TenantAccessDeniedException;
import com.nexbill.exception.TenantNotFoundException;
import com.nexbill.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurrentTenantService {

    private final TenantRepository tenantRepository;

    /**
     * Resolves the required current tenant ID from the thread context.
     * Throws TenantAccessDeniedException if no tenant is set.
     */
    public Long getRequiredTenantId() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            log.warn("Attempted to access tenant-scoped resource without an authenticated tenant context");
            throw new TenantAccessDeniedException("No authenticated tenant context established for this request");
        }
        return tenantId;
    }

    /**
     * Returns an Optional of the current tenant ID if available.
     */
    public Optional<Long> getCurrentTenantId() {
        return Optional.ofNullable(TenantContext.getTenantId());
    }

    /**
     * Retrieves the current Tenant entity from the database.
     * Throws TenantNotFoundException if not found or inactive.
     */
    public Tenant getRequiredCurrentTenant() {
        Long tenantId = getRequiredTenantId();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found for ID: " + tenantId));

        if (!Boolean.TRUE.equals(tenant.getIsActive()) || "INACTIVE".equalsIgnoreCase(tenant.getStatus())) {
            throw new TenantAccessDeniedException("Tenant account is deactivated or suspended");
        }

        return tenant;
    }

    /**
     * Validates that the requested targetTenantId matches the current authenticated tenant.
     * Protects against cross-tenant data access attempts.
     */
    public void validateTenantAccess(Long targetTenantId) {
        Long currentTenantId = getRequiredTenantId();
        if (targetTenantId == null || !currentTenantId.equals(targetTenantId)) {
            log.error("Cross-tenant access violation: Authenticated tenant {} attempted to access tenant {}",
                    currentTenantId, targetTenantId);
            throw new TenantAccessDeniedException("Cross-tenant access forbidden. You cannot access data outside your tenant domain.");
        }
    }
}
