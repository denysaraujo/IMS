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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlanLimitServiceTest {
    private final CompanyRepository companyRepository = mock(CompanyRepository.class);
    private final SubscriptionRepository subscriptionRepository = mock(SubscriptionRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final CustomerRepository customerRepository = mock(CustomerRepository.class);
    private final InventoryItemRepository inventoryItemRepository = mock(InventoryItemRepository.class);
    private final PlanLimitService service = new PlanLimitService(
            companyRepository,
            subscriptionRepository,
            userRepository,
            customerRepository,
            inventoryItemRepository
    );

    @AfterEach
    void clearTenant() {
        TenantContext.clear();
    }

    @Test
    void allowsUsageBelowThePlanLimit() {
        configureTenant(2, 5, SubscriptionStatus.ACTIVE, null);
        when(customerRepository.count()).thenReturn(4L);

        assertDoesNotThrow(service::assertCanAddCustomer);
        verify(companyRepository).findByIdForUpdate(2L);
    }

    @Test
    void rejectsUsageAtThePlanLimit() {
        configureTenant(2, 5, SubscriptionStatus.ACTIVE, null);
        when(customerRepository.count()).thenReturn(5L);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                service::assertCanAddCustomer
        );

        assertEquals(402, exception.getStatusCode().value());
    }

    @Test
    void rejectsAnExpiredTrial() {
        configureTenant(2, null, SubscriptionStatus.TRIALING, LocalDateTime.now().minusMinutes(1));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                service::assertCanAddUser
        );

        assertEquals(403, exception.getStatusCode().value());
    }

    private void configureTenant(
            long tenantId,
            Integer customerLimit,
            SubscriptionStatus status,
            LocalDateTime trialEndsAt) {
        TenantContext.setTenantId(tenantId);
        when(companyRepository.findByIdForUpdate(tenantId)).thenReturn(Optional.of(new Company()));
        Plan plan = new Plan();
        plan.setMaxCustomers(customerLimit);
        Subscription subscription = new Subscription();
        subscription.setStatus(status);
        subscription.setTrialEndsAt(trialEndsAt);
        subscription.setPlan(plan);
        when(subscriptionRepository.findFirstByStatusInOrderByIdDesc(
                List.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.TRIALING)))
                .thenReturn(Optional.of(subscription));
    }
}
