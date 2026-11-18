package com.br.api.controller;

import com.br.api.dto.saas.CheckoutDTO;
import com.br.api.dto.saas.SubscriptionDTO;
import com.br.api.dto.saas.PaymentDTO;
import com.br.api.service.saas.MercadoPagoClient;
import com.br.api.service.saas.MercadoPagoWebhookService;
import com.br.api.service.saas.SaasRegistrationService;
import com.br.api.service.saas.SubscriptionProvisioningService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.validation.annotation.Validated;

@RestController
@Validated
@RequestMapping("/api/billing")
public class BillingController {
    private final SubscriptionProvisioningService subscriptionService;
    private final SaasRegistrationService registrationService;
    private final MercadoPagoClient mercadoPagoClient;
    private final MercadoPagoWebhookService webhookService;

    public BillingController(
            SubscriptionProvisioningService subscriptionService,
            SaasRegistrationService registrationService,
            MercadoPagoClient mercadoPagoClient,
            MercadoPagoWebhookService webhookService) {
        this.subscriptionService = subscriptionService;
        this.registrationService = registrationService;
        this.mercadoPagoClient = mercadoPagoClient;
        this.webhookService = webhookService;
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('ADMIN')")
    public SubscriptionDTO currentSubscription() {
        return subscriptionService.currentSubscription();
    }

    @GetMapping("/payments")
    @PreAuthorize("hasRole('ADMIN')")
    public java.util.List<PaymentDTO> payments() {
        return subscriptionService.currentCompanyPayments();
    }

    @PostMapping("/subscribe")
    @PreAuthorize("hasRole('ADMIN')")
    public CheckoutDTO subscribe(@Valid @RequestBody SubscribeRequest request) {
        String email = subscriptionService.currentCompanyBillingEmail();
        return registrationService.subscribeCurrentCompany(request.planId(), email);
    }

    @PostMapping("/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public SubscriptionDTO cancel() {
        SubscriptionDTO subscription = subscriptionService.currentSubscription();
        if (subscription == null || subscription.providerSubscriptionId() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Não há assinatura ativa no Mercado Pago para cancelar.");
        }
        String providerStatus = mercadoPagoClient.cancelSubscription(subscription.providerSubscriptionId());
        if (!"cancelled".equalsIgnoreCase(providerStatus) && !"canceled".equalsIgnoreCase(providerStatus)) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "O Mercado Pago não confirmou o cancelamento.");
        }
        return subscriptionService.markCurrentSubscriptionCancelled();
    }

    @PostMapping("/webhook")
    public void webhook(
            @RequestBody JsonNode payload,
            @RequestParam(name = "data.id", required = false) String queryDataId,
            @RequestHeader(name = "x-signature", required = false) String signature,
            @RequestHeader(name = "x-request-id", required = false) String requestId) {
        webhookService.process(payload, queryDataId, signature, requestId);
    }

    public record SubscribeRequest(@NotNull Long planId) { }
}
