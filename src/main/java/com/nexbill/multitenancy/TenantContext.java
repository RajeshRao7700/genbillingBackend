package com.nexbill.multitenancy;

/**
 * Convenience alias for TenantContextHolder to support both naming conventions.
 */
public final class TenantContext {

    private TenantContext() {
    }

    public static void setTenant(TenantContextData tenantData) {
        TenantContextHolder.setTenant(tenantData);
    }

    public static void setTenantId(Long tenantId) {
        TenantContextHolder.setTenantId(tenantId);
    }

    public static TenantContextData getTenant() {
        return TenantContextHolder.getTenant();
    }

    public static Long getTenantId() {
        return TenantContextHolder.getTenantId();
    }

    public static void clear() {
        TenantContextHolder.clear();
    }
}
