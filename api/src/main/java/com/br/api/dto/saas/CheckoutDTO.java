package com.br.api.dto.saas;

public record CheckoutDTO(
        Long companyId,
        Long subscriptionId,
        String checkoutUrl,
        String status,
        java.time.LocalDateTime trialEndsAt
) {
}
