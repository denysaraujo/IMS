package com.br.api.service.saas;

import com.br.api.model.saas.Subscription;
import com.br.api.model.saas.SubscriptionStatus;
import com.br.api.repository.saas.SubscriptionRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SaasAccessServiceTest {
    private final SubscriptionRepository subscriptionRepository = mock(SubscriptionRepository.class);
    private final SaasAccessService service = new SaasAccessService(subscriptionRepository);

    @Test
    void allowsUnexpiredTrial() {
        Subscription trial = new Subscription();
        trial.setStatus(SubscriptionStatus.TRIALING);
        trial.setTrialEndsAt(LocalDateTime.now().plusDays(1));
        when(subscriptionRepository.findFirstByStatusInOrderByIdDesc(
                List.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.TRIALING)))
                .thenReturn(Optional.of(trial));

        assertTrue(service.hasActiveSubscription());
    }

    @Test
    void deniesExpiredTrialAndMissingSubscription() {
        Subscription trial = new Subscription();
        trial.setStatus(SubscriptionStatus.TRIALING);
        trial.setTrialEndsAt(LocalDateTime.now().minusSeconds(1));
        when(subscriptionRepository.findFirstByStatusInOrderByIdDesc(
                List.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.TRIALING)))
                .thenReturn(Optional.of(trial))
                .thenReturn(Optional.empty());

        assertFalse(service.hasActiveSubscription());
        assertFalse(service.hasActiveSubscription());
    }
}
