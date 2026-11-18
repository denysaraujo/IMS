package com.br.api.controller;

import com.br.api.dto.saas.CompanyAdminDTO;
import com.br.api.dto.saas.FeatureDTO;
import com.br.api.dto.saas.FeatureRequest;
import com.br.api.dto.saas.PlanDTO;
import com.br.api.dto.saas.PlanRequest;
import com.br.api.dto.saas.PaymentDTO;
import com.br.api.dto.saas.SignupRequest;
import com.br.api.dto.saas.SubscriptionDTO;
import com.br.api.model.saas.BillingEvent;
import com.br.api.service.saas.PlanManagementService;
import com.br.api.service.saas.SaasManagementService;
import com.br.api.service.saas.SaasRegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/saas")
public class SaasController {
    private final PlanManagementService planManagementService;
    private final SaasRegistrationService registrationService;
    private final SaasManagementService managementService;

    public SaasController(
            PlanManagementService planManagementService,
            SaasRegistrationService registrationService,
            SaasManagementService managementService) {
        this.planManagementService = planManagementService;
        this.registrationService = registrationService;
        this.managementService = managementService;
    }

    @GetMapping("/public/plans")
    public List<PlanDTO> publicPlans() {
        return planManagementService.listPlans(true);
    }

    @GetMapping("/public/features")
    public List<FeatureDTO> publicFeatures() {
        return planManagementService.listFeatures(true);
    }

    @PostMapping("/public/signup")
    public com.br.api.dto.saas.CheckoutDTO signup(@Valid @RequestBody SignupRequest request) {
        return registrationService.register(request);
    }

    @GetMapping("/admin/plans")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public List<PlanDTO> adminPlans() {
        return planManagementService.listPlans(false);
    }

    @PostMapping("/admin/plans")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public PlanDTO createPlan(@Valid @RequestBody PlanRequest request) {
        return planManagementService.createPlan(request);
    }

    @PutMapping("/admin/plans/{id}")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public PlanDTO updatePlan(@PathVariable Long id, @Valid @RequestBody PlanRequest request) {
        return planManagementService.updatePlan(id, request);
    }

    @GetMapping("/admin/features")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public List<FeatureDTO> adminFeatures() {
        return planManagementService.listFeatures(false);
    }

    @PostMapping("/admin/features")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public FeatureDTO createFeature(@Valid @RequestBody FeatureRequest request) {
        return planManagementService.createFeature(request);
    }

    @PutMapping("/admin/features/{id}")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public FeatureDTO updateFeature(@PathVariable Long id, @Valid @RequestBody FeatureRequest request) {
        return planManagementService.updateFeature(id, request);
    }

    @GetMapping("/admin/companies")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public List<CompanyAdminDTO> companies() {
        return managementService.listCompanies();
    }

    @PutMapping("/admin/companies/{id}/enabled")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public CompanyAdminDTO setCompanyEnabled(@PathVariable Long id, @RequestBody Map<String, Boolean> request) {
        Boolean enabled = request.get("enabled");
        if (enabled == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe se a empresa deve ficar ativa.");
        }
        return managementService.setCompanyEnabled(id, enabled);
    }

    @GetMapping("/admin/subscriptions")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public List<SubscriptionDTO> subscriptions() {
        return managementService.listSubscriptions();
    }

    @GetMapping("/admin/billing-events")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public List<BillingEvent> billingEvents() {
        return managementService.listBillingEvents();
    }

    @GetMapping("/admin/payments")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public List<PaymentDTO> payments() {
        return managementService.listPayments();
    }
}
