package com.br.api.service.saas;

import com.br.api.model.saas.SubscriptionStatus;
import com.br.api.repository.saas.SubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SaasAccessService {
    private static final List<SubscriptionStatus> ACCESSIBLE_STATUSES =
            List.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.TRIALING);

    private final SubscriptionRepository subscriptionRepository;

    public SaasAccessService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Transactional(readOnly = true)
    public boolean hasActiveSubscription() {
        return subscriptionRepository.findFirstByStatusInOrderByIdDesc(ACCESSIBLE_STATUSES)
                .map(subscription -> subscription.getStatus() == SubscriptionStatus.ACTIVE
                        || subscription.getTrialEndsAt() != null
                                && subscription.getTrialEndsAt().isAfter(LocalDateTime.now()))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean hasFeature(String featureCode) {
        return subscriptionRepository.findFirstByStatusInOrderByIdDesc(ACCESSIBLE_STATUSES)
                .filter(subscription -> subscription.getStatus() == SubscriptionStatus.ACTIVE
                        || subscription.getTrialEndsAt() != null
                                && subscription.getTrialEndsAt().isAfter(LocalDateTime.now()))
                .map(subscription -> subscription.getPlan().getFeatures().stream()
                        .anyMatch(feature -> feature.getCode().equals(featureCode) && feature.isActive()))
                .orElse(false);
    }
}
