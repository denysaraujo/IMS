package com.br.api.service.saas;

import com.br.api.model.saas.BillingEvent;
import com.br.api.model.saas.SaasPayment;
import com.br.api.model.saas.Subscription;
import com.br.api.model.saas.SubscriptionStatus;
import com.br.api.repository.saas.BillingEventRepository;
import com.br.api.repository.saas.SubscriptionRepository;
import com.br.api.repository.saas.SaasPaymentRepository;
import com.br.api.repository.UserRepository;
import com.br.api.tenant.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class BillingWebhookPersistenceService {
    private final BillingEventRepository billingEventRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final SaasPaymentRepository paymentRepository;

    public BillingWebhookPersistenceService(
            BillingEventRepository billingEventRepository,
            SubscriptionRepository subscriptionRepository,
            UserRepository userRepository,
            SaasPaymentRepository paymentRepository) {
        this.billingEventRepository = billingEventRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public void recordPaymentAndEvent(
            String eventId,
            String eventType,
            MercadoPagoClient.ProviderPayment providerPayment,
            String externalReference) {
        TenantContext.setTenantId(0L);
        try {
            if (billingEventRepository.existsByProviderEventId(eventId)) {
                return;
            }
            Subscription subscription = subscriptionRepository
                    .findByProviderSubscriptionId(providerPayment.preapprovalId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assinatura da cobrança não encontrada."));
            if (externalReference != null
                    && !externalReference.equals(subscription.getCompany().getId().toString())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Referência da cobrança inválida.");
            }
            if (providerPayment.amount().signum() < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valor de cobrança inválido.");
            }

            SaasPayment payment = paymentRepository.findByProviderPaymentId(providerPayment.id())
                    .orElseGet(SaasPayment::new);
            payment.setCompany(subscription.getCompany());
            payment.setSubscription(subscription);
            payment.setProviderPaymentId(providerPayment.id());
            payment.setAmount(providerPayment.amount());
            payment.setCurrency(providerPayment.currency());
            payment.setStatus(providerPayment.status());
            payment.setPaidAt(providerPayment.paidAt());
            paymentRepository.save(payment);

            BillingEvent event = new BillingEvent();
            event.setProviderEventId(eventId);
            event.setProviderSubscriptionId(providerPayment.preapprovalId());
            event.setType(eventType);
            event.setStatus(providerPayment.status());
            billingEventRepository.save(event);
        } finally {
            TenantContext.clear();
        }
    }

    @Transactional
    public void recordAndUpdate(
            String eventId,
            String eventType,
            String providerSubscriptionId,
            String providerStatus,
            String externalReference) {
        TenantContext.setTenantId(0L);
        try {
            if (billingEventRepository.existsByProviderEventId(eventId)) {
                return;
            }
            Subscription subscription = subscriptionRepository
                    .findByProviderSubscriptionId(providerSubscriptionId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assinatura não encontrada."));
            if (externalReference != null
                    && !externalReference.equals(subscription.getCompany().getId().toString())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Referência da assinatura inválida.");
            }

            SubscriptionStatus status = mapStatus(providerStatus);
            subscription.setStatus(status);
            subscription.setUpdatedAt(LocalDateTime.now());
            if (status == SubscriptionStatus.ACTIVE && subscription.getCompany().getEmail() != null) {
                userRepository.findByEmailIgnoreCase(subscription.getCompany().getEmail())
                        .filter(user -> user.getTenantId().equals(subscription.getCompany().getId()))
                        .ifPresent(user -> {
                            user.setEnabled(true);
                            userRepository.save(user);
                        });
            }
            BillingEvent event = new BillingEvent();
            event.setProviderEventId(eventId);
            event.setProviderSubscriptionId(providerSubscriptionId);
            event.setType(eventType);
            event.setStatus(providerStatus);
            billingEventRepository.save(event);
        } finally {
            TenantContext.clear();
        }
    }

    private SubscriptionStatus mapStatus(String providerStatus) {
        return switch (providerStatus.toLowerCase()) {
            case "authorized", "active" -> SubscriptionStatus.ACTIVE;
            case "paused" -> SubscriptionStatus.PAUSED;
            case "cancelled", "canceled" -> SubscriptionStatus.CANCELLED;
            case "payment_failed", "rejected" -> SubscriptionStatus.PAYMENT_FAILED;
            case "expired" -> SubscriptionStatus.EXPIRED;
            default -> SubscriptionStatus.PENDING_PAYMENT;
        };
    }
}
