package com.br.api.repository.saas;

import com.br.api.model.saas.SaasPayment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SaasPaymentRepository extends JpaRepository<SaasPayment, Long> {
    @EntityGraph(attributePaths = {"company", "subscription", "subscription.plan"})
    List<SaasPayment> findByCompany_IdOrderByCreatedAtDesc(Long companyId);

    @EntityGraph(attributePaths = {"company", "subscription", "subscription.plan"})
    List<SaasPayment> findAllByOrderByCreatedAtDesc();

    Optional<SaasPayment> findByProviderPaymentId(String providerPaymentId);
}
