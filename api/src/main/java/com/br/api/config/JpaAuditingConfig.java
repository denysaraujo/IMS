package com.br.api.config;

import com.br.api.tenant.TenantIdentifierResolver;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
    @Bean
    HibernatePropertiesCustomizer tenantIdentifierResolverCustomizer(
            TenantIdentifierResolver tenantIdentifierResolver) {
        return properties -> properties.put(
                "hibernate.tenant_identifier_resolver",
                tenantIdentifierResolver
        );
    }
}