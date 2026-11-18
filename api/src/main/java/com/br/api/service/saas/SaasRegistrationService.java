package com.br.api.service.saas;

import com.br.api.dto.saas.CheckoutDTO;
import com.br.api.dto.saas.SignupRequest;
import com.br.api.dto.saas.SubscriptionDTO;
import com.br.api.model.Company;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SaasRegistrationService {
    private final CompanyProvisioningService companyProvisioningService;
    private final SubscriptionProvisioningService subscriptionProvisioningService;
    private final MercadoPagoClient mercadoPagoClient;

    public SaasRegistrationService(
            CompanyProvisioningService companyProvisioningService,
            SubscriptionProvisioningService subscriptionProvisioningService,
            MercadoPagoClient mercadoPagoClient) {
        this.companyProvisioningService = companyProvisioningService;
        this.subscriptionProvisioningService = subscriptionProvisioningService;
        this.mercadoPagoClient = mercadoPagoClient;
    }

    public CheckoutDTO register(SignupRequest request) {
        Company company = companyProvisioningService.createCompany(request);
        try {
            SubscriptionDTO subscription = subscriptionProvisioningService
                    .createOwnerAndTrialSubscription(company.getId(), request);
            return new CheckoutDTO(
                    company.getId(),
                    subscription.id(),
                    null,
                    subscription.status(),
                    subscription.trialEndsAt()
            );
        } catch (RuntimeException exception) {
            try {
                companyProvisioningService.deleteUnpaidCompany(company.getId());
            } catch (RuntimeException cleanupException) {
                exception.addSuppressed(cleanupException);
            }
            throw exception;
        }
    }

    public CheckoutDTO subscribeCurrentCompany(Long planId, String email) {
        if (!mercadoPagoClient.isConfigured()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Integração Mercado Pago não configurada.");
        }
        SubscriptionDTO subscription = subscriptionProvisioningService
                .createPendingSubscriptionForCurrentCompany(planId);
        if (subscription.checkoutUrl() != null && !subscription.checkoutUrl().isBlank()) {
            return new CheckoutDTO(
                    subscription.companyId(),
                    subscription.id(),
                    subscription.checkoutUrl(),
                    subscription.status(),
                    subscription.trialEndsAt()
            );
        }
        try {
            return createCheckout(subscription.companyId(), email, subscription);
        } catch (RestClientException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Não foi possível iniciar a assinatura no Mercado Pago. Tente novamente.",
                    exception
            );
        }
    }

    private CheckoutDTO createCheckout(Long companyId, String email, SubscriptionDTO subscription) {
        com.br.api.model.saas.Plan plan = new com.br.api.model.saas.Plan();
        plan.setName(subscription.planName());
        plan.setAmount(subscription.amount());
        plan.setCurrency(subscription.currency());
        plan.setFrequency(subscription.frequency());
        plan.setFrequencyType(subscription.frequencyType());
        MercadoPagoClient.Checkout checkout = mercadoPagoClient.createSubscription(
                plan,
                email,
                companyId
        );
        subscriptionProvisioningService.saveCheckout(companyId, subscription.id(), checkout);
        return new CheckoutDTO(companyId, subscription.id(), checkout.url(), "PENDING_PAYMENT", null);
    }
}
