package com.br.api.tenant;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TenantContextTest {
    private final TenantIdentifierResolver resolver = new TenantIdentifierResolver();

    @AfterEach
    void clearTenant() {
        TenantContext.clear();
    }

    @Test
    void defaultsToRootTenantWhenNoRequestTenantIsSet() {
        assertEquals(0L, resolver.resolveCurrentTenantIdentifier());
        assertTrue(resolver.isRoot(resolver.resolveCurrentTenantIdentifier()));
    }

    @Test
    void resolvesTenantFromRequestContext() {
        TenantContext.setTenantId(42L);

        assertEquals(42L, resolver.resolveCurrentTenantIdentifier());
        assertFalse(resolver.isRoot(resolver.resolveCurrentTenantIdentifier()));
    }

    @Test
    void nullTenantResolvesToRootTenant() {
        TenantContext.setTenantId(null);

        assertEquals(0L, resolver.resolveCurrentTenantIdentifier());
        assertTrue(resolver.isRoot(resolver.resolveCurrentTenantIdentifier()));
    }
}
