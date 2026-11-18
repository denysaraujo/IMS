package com.br.api.repository.saas;

import com.br.api.model.saas.BillingEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BillingEventRepository extends JpaRepository<BillingEvent, Long> {
    boolean existsByProviderEventId(String providerEventId);
    List<BillingEvent> findTop100ByOrderByReceivedAtDesc();
}
