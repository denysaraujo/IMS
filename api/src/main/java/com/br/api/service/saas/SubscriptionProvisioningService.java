package com.br.api.service.saas;

import com.br.api.dto.saas.CheckoutDTO;
import com.br.api.dto.saas.SignupRequest;
import com.br.api.dto.saas.SubscriptionDTO;
import com.br.api.dto.saas.PaymentDTO;
import com.br.api.model.Company;
import com.br.api.model.Role;
import com.br.api.model.User;
import com.br.api.model.saas.Plan;
import com.br.api.model.saas.Subscription;
import com.br.api.model.saas.SubscriptionStatus;
import com.br.api.repository.CompanyRepository;
import com.br.api.repository.UserRepository;
import com.br.api.repository.saas.PlanRepository;
import com.br.api.repository.saas.SubscriptionRepository;
import com.br.api.repository.saas.SaasPaymentRepository;
import com.br.api.tenant.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SubscriptionProvisioningService {
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PasswordEncoder passwordEncoder;
    private final SaasPaymentRepository paymentRepository;
    private final int trialDays;

    public SubscriptionProvisioningService(
            CompanyRepository companyRepository,
            UserRepository userRepository,
            PlanRepository planRepository,
            SubscriptionRepository subscriptionRepository,
            SaasPaymentRepository paymentRepository,
            PasswordEncoder passwordEncoder,
            @Value("${saas.trial-days:14}") int trialDays) {
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.planRepository = planRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.paymentRepository = paymentRepository;
        this.passwordEncoder = passwordEncoder;
        if (trialDays <= 0) {
            throw new IllegalArgumentException("A duração do trial deve ser maior que zero.");
        }
        this.trialDays = trialDays;
    }

    @Transactional
    public SubscriptionDTO createOwnerAndTrialSubscription(Long companyId, SignupRequest request) {
        TenantContext.setTenantId(companyId);
        try {
            Company company = companyRepository.findById(companyId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada."));
            Plan plan = activePlan(request.planId());

            User owner = new User();
            owner.setNomeCompleto(request.ownerName().trim());
            owner.setUsername(request.username().trim());
            owner.setEmail(request.email().trim().toLowerCase());
            owner.setPassword(passwordEncoder.encode(request.password()));
            owner.setRole(Role.ADMIN);
            owner.setEnabled(true);
            owner.setAccountNonLocked(true);
            owner.setAccountNonExpired(true);
            owner.setCredentialsNonExpired(true);
            owner.setLoginAttempts(0);
            userRepository.save(owner);

            Subscription subscription = newSubscription(company, plan);
            subscription.setStatus(SubscriptionStatus.TRIALING);
            subscription.setTrialEndsAt(LocalDateTime.now().plusDays(trialDays));
            return toDTO(subscriptionRepository.save(subscription));
        } finally {
            TenantContext.clear();
        }
    }

    @Transactional
    public SubscriptionDTO createPendingSubscriptionForCurrentCompany(Long planId) {
        Long companyId = TenantContext.getTenantId();
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada."));
        Plan plan = activePlan(planId);
        List<Subscription> subscriptions = subscriptionRepository.findAll();
        if (subscriptions.stream().anyMatch(subscription -> subscription.getStatus() == SubscriptionStatus.ACTIVE)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cancele a assinatura atual antes de trocar de plano.");
        }
        Subscription latest = subscriptionRepository.findFirstByOrderByIdDesc().orElse(null);
        if (latest != null && latest.getStatus() == SubscriptionStatus.PENDING_PAYMENT) {
            if (!latest.getPlan().getId().equals(planId) && latest.getProviderSubscriptionId() != null) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Já existe um checkout pendente para outro plano. Conclua ou cancele esse pagamento primeiro."
                );
            }
            if (latest.getPlan().getId().equals(planId)) {
                return toDTO(latest);
            }
            latest.setPlan(plan);
            latest.setAmount(plan.getAmount());
            latest.setCurrency(plan.getCurrency());
            latest.setUpdatedAt(LocalDateTime.now());
            return toDTO(latest);
        }
        Subscription subscription = subscriptionRepository.save(newSubscription(company, plan));
        return toDTO(subscription);
    }

    @Transactional(readOnly = true)
    public String currentCompanyBillingEmail() {
        return companyRepository.findById(TenantContext.getTenantId())
                .map(Company::getEmail)
                .filter(email -> email != null && !email.isBlank())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Cadastre um e-mail de cobrança válido nos dados da empresa."
                ));
    }

    @Transactional
    public void saveCheckout(Long companyId, Long subscriptionId, MercadoPagoClient.Checkout checkout) {
        TenantContext.setTenantId(companyId);
        try {
            Subscription subscription = subscriptionRepository.findById(subscriptionId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assinatura não encontrada."));
            subscription.setProviderSubscriptionId(checkout.id());
            subscription.setCheckoutUrl(checkout.url());
            subscription.setUpdatedAt(LocalDateTime.now());
        } finally {
            TenantContext.clear();
        }
    }

    @Transactional(readOnly = true)
    public SubscriptionDTO currentSubscription() {
        return subscriptionRepository.findFirstByOrderByIdDesc()
                .map(SubscriptionProvisioningService::toDTO)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<PaymentDTO> currentCompanyPayments() {
        return paymentRepository.findByCompany_IdOrderByCreatedAtDesc(TenantContext.getTenantId())
                .stream().map(PaymentDTO::new).toList();
    }

    @Transactional
    public SubscriptionDTO markCurrentSubscriptionCancelled() {
        Subscription subscription = subscriptionRepository.findFirstByOrderByIdDesc()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nenhuma assinatura foi encontrada."));
        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setUpdatedAt(LocalDateTime.now());
        return toDTO(subscription);
    }

    private Plan activePlan(Long planId) {
        Plan plan = planRepository.findWithFeaturesById(planId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plano não encontrado."));
        if (!plan.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Este plano não está disponível.");
        }
        return plan;
    }

    private Subscription newSubscription(Company company, Plan plan) {
        Subscription subscription = new Subscription();
        subscription.setCompany(company);
        subscription.setPlan(plan);
        subscription.setAmount(plan.getAmount());
        subscription.setCurrency(plan.getCurrency());
        subscription.setStatus(SubscriptionStatus.PENDING_PAYMENT);
        return subscription;
    }

    static SubscriptionDTO toDTO(Subscription subscription) {
        return new SubscriptionDTO(subscription);
    }
}
