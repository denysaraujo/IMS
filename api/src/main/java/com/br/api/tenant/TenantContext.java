package com.br.api.tenant;

public final class TenantContext {
    private static final long ROOT_TENANT_ID = 0L;
    private static final ThreadLocal<Long> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {
    }

    public static Long getTenantId() {
        Long tenantId = CURRENT_TENANT.get();
        return tenantId == null ? ROOT_TENANT_ID : tenantId;
    }

    public static void setTenantId(Long tenantId) {
        CURRENT_TENANT.set(tenantId == null ? ROOT_TENANT_ID : tenantId);
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
