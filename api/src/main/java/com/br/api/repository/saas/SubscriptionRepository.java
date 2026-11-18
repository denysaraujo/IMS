package com.br.api.repository.saas;

import com.br.api.model.saas.Subscription;
import com.br.api.model.saas.SubscriptionStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Collection;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    @EntityGraph(attributePaths = {"plan", "plan.features"})
    Optional<Subscription> findFirstByOrderByIdDesc();

    @EntityGraph(attributePaths = {"plan", "plan.features"})
    Optional<Subscription> findFirstByStatusInOrderByIdDesc(Collection<SubscriptionStatus> statuses);

    @EntityGraph(attributePaths = {"company", "plan"})
    List<Subscription> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"company", "plan"})
    Optional<Subscription> findByProviderSubscriptionId(String providerSubscriptionId);

    boolean existsByProviderSubscriptionId(String providerSubscriptionId);
    boolean existsByProviderSubscriptionIdAndStatus(String providerSubscriptionId, SubscriptionStatus status);
}
