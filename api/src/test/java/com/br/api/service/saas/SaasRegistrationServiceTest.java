package com.br.api.service.saas;

import com.br.api.dto.saas.SignupRequest;
import com.br.api.dto.saas.SubscriptionDTO;
import com.br.api.model.Company;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class SaasRegistrationServiceTest {
    private final CompanyProvisioningService companyProvisioningService = mock(CompanyProvisioningService.class);
    private final SubscriptionProvisioningService subscriptionProvisioningService = mock(SubscriptionProvisioningService.class);
    private final MercadoPagoClient mercadoPagoClient = mock(MercadoPagoClient.class);
    private final SaasRegistrationService service = new SaasRegistrationService(
            companyProvisioningService,
            subscriptionProvisioningService,
            mercadoPagoClient
    );

    @Test
    void signupStartsTrialWithoutRequiringImmediatePaymentCheckout() {
        SignupRequest request = new SignupRequest(
                "Loja Exemplo",
                "Ana Silva",
                null,
                "ana@example.com",
                "ana.silva",
                "senha-segura-123",
                1L
        );
        Company company = mock(Company.class);
        when(company.getId()).thenReturn(7L);
        when(companyProvisioningService.createCompany(request)).thenReturn(company);
        LocalDateTime trialEndsAt = LocalDateTime.now().plusDays(14);
        SubscriptionDTO subscription = new SubscriptionDTO(
                9L, 7L, "Loja Exemplo", 1L, "Essencial",
                BigDecimal.valueOf(99.90), "BRL", 1, "months",
                "TRIALING", "MERCADO_PAGO", null, null, LocalDateTime.now(), trialEndsAt
        );
        when(subscriptionProvisioningService.createOwnerAndTrialSubscription(7L, request))
                .thenReturn(subscription);

        var result = service.register(request);

        assertEquals("TRIALING", result.status());
        assertEquals(trialEndsAt, result.trialEndsAt());
        assertNull(result.checkoutUrl());
        verifyNoInteractions(mercadoPagoClient);
        verify(subscriptionProvisioningService).createOwnerAndTrialSubscription(7L, request);
    }
}
