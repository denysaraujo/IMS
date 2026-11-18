package com.br.api.service.saas;

import com.br.api.model.Company;
import com.br.api.model.saas.Plan;
import com.br.api.model.saas.Subscription;
import com.br.api.model.saas.SubscriptionStatus;
import com.br.api.repository.CompanyRepository;
import com.br.api.repository.UserRepository;
import com.br.api.repository.customer.CustomerRepository;
import com.br.api.repository.inventory.InventoryItemRepository;
import com.br.api.repository.saas.SubscriptionRepository;
import com.br.api.tenant.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;
import java.util.function.LongSupplier;

@Service
public class PlanLimitService {
    private static final List<SubscriptionStatus> ACCESSIBLE_STATUSES =
            List.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.TRIALING);

    private final CompanyRepository companyRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final InventoryItemRepository inventoryItemRepository;

    public PlanLimitService(
            CompanyRepository companyRepository,
            SubscriptionRepository subscriptionRepository,
            UserRepository userRepository,
            CustomerRepository customerRepository,
            InventoryItemRepository inventoryItemRepository) {
        this.companyRepository = companyRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.inventoryItemRepository = inventoryItemRepository;
    }

    @Transactional
    public void assertCanAddUser() {
        assertWithinLimit("usuários", () -> userRepository.countByRoleNot(com.br.api.model.Role.PLATFORM_ADMIN), Plan::getMaxUsers);
    }

    @Transactional
    public void assertCanAddCustomer() {
        assertWithinLimit("clientes", customerRepository::count, Plan::getMaxCustomers);
    }

    @Transactional
    public void assertCanAddInventoryItem() {
        assertWithinLimit("itens de estoque", inventoryItemRepository::count, Plan::getMaxInventoryItems);
    }

    private void assertWithinLimit(
            String resource,
            LongSupplier countCurrentUsage,
            Function<Plan, Integer> limitForPlan) {
        Long tenantId = TenantContext.getTenantId();
        companyRepository.findByIdForUpdate(tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Empresa não encontrada."));

        Subscription subscription = subscriptionRepository
                .findFirstByStatusInOrderByIdDesc(ACCESSIBLE_STATUSES)
                .filter(candidate -> candidate.getStatus() == SubscriptionStatus.ACTIVE
                        || candidate.getTrialEndsAt() != null
                                && candidate.getTrialEndsAt().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Assinatura inativa. Regularize o plano antes de cadastrar novos dados."
                ));

        Integer limit = limitForPlan.apply(subscription.getPlan());
        if (limit != null && countCurrentUsage.getAsLong() >= limit) {
            throw new ResponseStatusException(
                    HttpStatus.PAYMENT_REQUIRED,
                    "O limite de " + resource + " do plano foi atingido. Altere seu plano para continuar."
            );
        }
    }
}
