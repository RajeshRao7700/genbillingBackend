package com.nexbill.multitenancy;

import lombok.extern.slf4j.Slf4j;

/**
 * ThreadLocal-based Tenant Context holder.
 * Stores the isolated tenant metadata strictly resolved from the authenticated SecurityContext.
 * Guaranteed to be cleared after each request by the security filter.
 */
@Slf4j
public final class TenantContextHolder {

    private static final ThreadLocal<TenantContextData> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContextHolder() {
        // Private constructor for utility class
    }

    public static void setTenant(TenantContextData tenantData) {
        if (tenantData != null) {
            log.trace("Setting TenantContext for tenantId={}", tenantData.getTenantId());
        }
        CURRENT_TENANT.set(tenantData);
    }

    public static void setTenantId(Long tenantId) {
        if (tenantId == null) {
            CURRENT_TENANT.remove();
        } else {
            TenantContextData data = CURRENT_TENANT.get();
            if (data == null) {
                data = TenantContextData.builder().tenantId(tenantId).build();
            } else {
                data.setTenantId(tenantId);
            }
            CURRENT_TENANT.set(data);
        }
    }

    public static TenantContextData getTenant() {
        return CURRENT_TENANT.get();
    }

    public static Long getTenantId() {
        TenantContextData data = CURRENT_TENANT.get();
        return data != null ? data.getTenantId() : null;
    }

    public static void clear() {
        log.trace("Clearing TenantContext");
        CURRENT_TENANT.remove();
    }
}
