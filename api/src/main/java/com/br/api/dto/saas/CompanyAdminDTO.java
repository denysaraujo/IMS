package com.br.api.dto.saas;

import com.br.api.model.Company;

import java.time.LocalDateTime;

public record CompanyAdminDTO(
        Long id,
        String name,
        String document,
        String email,
        boolean enabled,
        LocalDateTime createdAt,
        String subscriptionStatus,
        String planName,
        long userCount,
        long customerCount,
        long inventoryItemCount,
        long saleCount
) {
    public CompanyAdminDTO(
            Company company,
            SubscriptionDTO subscription,
            long userCount,
            long customerCount,
            long inventoryItemCount,
            long saleCount) {
        this(
                company.getId(),
                company.getName(),
                company.getDocument(),
                company.getEmail(),
                company.isEnabled(),
                company.getCreatedAt(),
                subscription == null ? null : subscription.status(),
                subscription == null ? null : subscription.planName(),
                userCount,
                customerCount,
                inventoryItemCount,
                saleCount
        );
    }
}
