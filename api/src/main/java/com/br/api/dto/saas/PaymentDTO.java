package com.br.api.dto.saas;

import com.br.api.model.saas.SaasPayment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentDTO(
        Long id,
        Long companyId,
        String companyName,
        Long subscriptionId,
        String planName,
        String providerPaymentId,
        BigDecimal amount,
        String currency,
        String status,
        LocalDateTime paidAt,
        LocalDateTime createdAt
) {
    public PaymentDTO(SaasPayment payment) {
        this(
                payment.getId(),
                payment.getCompany().getId(),
                payment.getCompany().getName(),
                payment.getSubscription().getId(),
                payment.getSubscription().getPlan().getName(),
                payment.getProviderPaymentId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus(),
                payment.getPaidAt(),
                payment.getCreatedAt()
        );
    }
}
