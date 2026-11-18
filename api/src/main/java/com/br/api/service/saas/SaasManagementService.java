package com.br.api.service.saas;

import com.br.api.dto.saas.CompanyAdminDTO;
import com.br.api.dto.saas.SubscriptionDTO;
import com.br.api.dto.saas.PaymentDTO;
import com.br.api.model.Company;
import com.br.api.model.saas.BillingEvent;
import com.br.api.repository.CompanyRepository;
import com.br.api.repository.UserRepository;
import com.br.api.repository.customer.CustomerRepository;
import com.br.api.repository.inventory.InventoryItemRepository;
import com.br.api.repository.sales.SaleRepository;
import com.br.api.repository.saas.BillingEventRepository;
import com.br.api.repository.saas.SubscriptionRepository;
import com.br.api.repository.saas.SaasPaymentRepository;
import com.br.api.tenant.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SaasManagementService {
    private final CompanyRepository companyRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final BillingEventRepository billingEventRepository;
    private final SaasPaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final SaleRepository saleRepository;

    public SaasManagementService(
            CompanyRepository companyRepository,
            SubscriptionRepository subscriptionRepository,
            BillingEventRepository billingEventRepository,
            SaasPaymentRepository paymentRepository,
            UserRepository userRepository,
            CustomerRepository customerRepository,
            InventoryItemRepository inventoryItemRepository,
            SaleRepository saleRepository) {
        this.companyRepository = companyRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.billingEventRepository = billingEventRepository;
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.saleRepository = saleRepository;
    }

    @Transactional(readOnly = true)
    public List<CompanyAdminDTO> listCompanies() {
        TenantContext.setTenantId(0L);
        try {
            Map<Long, SubscriptionDTO> latestByCompany = new HashMap<>();
            Map<Long, Long> usersByCompany = countByCompany(userRepository.countAllByCompany());
            Map<Long, Long> customersByCompany = countByCompany(customerRepository.countAllByCompany());
            Map<Long, Long> itemsByCompany = countByCompany(inventoryItemRepository.countAllByCompany());
            Map<Long, Long> salesByCompany = countByCompany(saleRepository.countAllByCompany());
            for (var subscription : subscriptionRepository.findAllByOrderByCreatedAtDesc()) {
                latestByCompany.putIfAbsent(
                        subscription.getCompany().getId(),
                        new SubscriptionDTO(subscription)
                );
            }
            return companyRepository.findAll().stream()
                    .map(company -> new CompanyAdminDTO(
                            company,
                            latestByCompany.get(company.getId()),
                            usersByCompany.getOrDefault(company.getId(), 0L),
                            customersByCompany.getOrDefault(company.getId(), 0L),
                            itemsByCompany.getOrDefault(company.getId(), 0L),
                            salesByCompany.getOrDefault(company.getId(), 0L)))
                    .toList();
        } finally {
            TenantContext.clear();
        }
    }

    @Transactional(readOnly = true)
    public List<SubscriptionDTO> listSubscriptions() {
        TenantContext.setTenantId(0L);
        try {
            return subscriptionRepository.findAllByOrderByCreatedAtDesc()
                    .stream().map(SubscriptionDTO::new).toList();
        } finally {
            TenantContext.clear();
        }
    }

    @Transactional(readOnly = true)
    public List<BillingEvent> listBillingEvents() {
        return billingEventRepository.findTop100ByOrderByReceivedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<PaymentDTO> listPayments() {
        TenantContext.setTenantId(0L);
        try {
            return paymentRepository.findAllByOrderByCreatedAtDesc()
                    .stream().map(PaymentDTO::new).toList();
        } finally {
            TenantContext.clear();
        }
    }

    @Transactional
    public CompanyAdminDTO setCompanyEnabled(Long companyId, boolean enabled) {
        TenantContext.setTenantId(0L);
        try {
            Company company = companyRepository.findById(companyId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada."));
            company.setEnabled(enabled);
            SubscriptionDTO latest = subscriptionRepository.findAllByOrderByCreatedAtDesc()
                    .stream()
                    .filter(subscription -> subscription.getCompany().getId().equals(companyId))
                    .findFirst()
                    .map(SubscriptionDTO::new)
                    .orElse(null);
            long userCount = countByCompany(userRepository.countAllByCompany()).getOrDefault(companyId, 0L);
            long customerCount = countByCompany(customerRepository.countAllByCompany()).getOrDefault(companyId, 0L);
            long itemCount = countByCompany(inventoryItemRepository.countAllByCompany()).getOrDefault(companyId, 0L);
            long saleCount = countByCompany(saleRepository.countAllByCompany()).getOrDefault(companyId, 0L);
            return new CompanyAdminDTO(
                    companyRepository.save(company),
                    latest,
                    userCount,
                    customerCount,
                    itemCount,
                    saleCount);
        } finally {
            TenantContext.clear();
        }
    }

    private Map<Long, Long> countByCompany(List<Object[]> result) {
        Map<Long, Long> counts = new HashMap<>();
        for (Object[] row : result) {
            counts.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
        }
        return counts;
    }
}
