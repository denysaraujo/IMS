package com.br.api.dto.saas;

import com.br.api.model.saas.Subscription;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SubscriptionDTO(
        Long id,
        Long companyId,
        String companyName,
        Long planId,
        String planName,
        BigDecimal amount,
        String currency,
        Integer frequency,
        String frequencyType,
        String status,
        String provider,
        String providerSubscriptionId,
        String checkoutUrl,
        LocalDateTime createdAt,
        LocalDateTime trialEndsAt
) {
    public SubscriptionDTO(Subscription subscription) {
        this(
                subscription.getId(),
                subscription.getCompany() == null ? subscription.getTenantId() : subscription.getCompany().getId(),
                subscription.getCompany() == null ? null : subscription.getCompany().getName(),
                subscription.getPlan().getId(),
                subscription.getPlan().getName(),
                subscription.getAmount(),
                subscription.getCurrency(),
                subscription.getPlan().getFrequency(),
                subscription.getPlan().getFrequencyType(),
                subscription.getStatus().name(),
                subscription.getProvider(),
                subscription.getProviderSubscriptionId(),
                subscription.getCheckoutUrl(),
                subscription.getCreatedAt(),
                subscription.getTrialEndsAt()
        );
    }
}
