package com.br.api.service.saas;

import com.br.api.dto.saas.FeatureDTO;
import com.br.api.dto.saas.FeatureRequest;
import com.br.api.dto.saas.PlanDTO;
import com.br.api.dto.saas.PlanRequest;
import com.br.api.model.saas.Feature;
import com.br.api.model.saas.Plan;
import com.br.api.repository.saas.FeatureRepository;
import com.br.api.repository.saas.PlanRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashSet;
import java.util.List;

@Service
public class PlanManagementService {
    private final PlanRepository planRepository;
    private final FeatureRepository featureRepository;

    public PlanManagementService(PlanRepository planRepository, FeatureRepository featureRepository) {
        this.planRepository = planRepository;
        this.featureRepository = featureRepository;
    }

    @Transactional(readOnly = true)
    public List<PlanDTO> listPlans(boolean activeOnly) {
        List<Plan> plans = activeOnly
                ? planRepository.findByActiveTrueOrderByAmountAsc()
                : planRepository.findAllByOrderByAmountAsc();
        return plans.stream().map(PlanDTO::new).toList();
    }

    @Transactional
    public PlanDTO createPlan(PlanRequest request) {
        if (planRepository.existsByCode(request.code())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um plano com esse código.");
        }
        Plan plan = new Plan();
        applyPlanRequest(plan, request);
        return new PlanDTO(planRepository.save(plan));
    }

    @Transactional
    public PlanDTO updatePlan(Long id, PlanRequest request) {
        Plan plan = planRepository.findWithFeaturesById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plano não encontrado."));
        if (planRepository.existsByCodeAndIdNot(request.code(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um plano com esse código.");
        }
        applyPlanRequest(plan, request);
        return new PlanDTO(planRepository.save(plan));
    }

    @Transactional(readOnly = true)
    public List<FeatureDTO> listFeatures(boolean activeOnly) {
        List<Feature> features = activeOnly
                ? featureRepository.findByActiveTrueOrderByNameAsc()
                : featureRepository.findAllByOrderByNameAsc();
        return features.stream().map(FeatureDTO::new).toList();
    }

    @Transactional
    public FeatureDTO createFeature(FeatureRequest request) {
        if (featureRepository.existsByCode(request.code())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma funcionalidade com esse código.");
        }
        Feature feature = new Feature();
        applyFeatureRequest(feature, request);
        return new FeatureDTO(featureRepository.save(feature));
    }

    @Transactional
    public FeatureDTO updateFeature(Long id, FeatureRequest request) {
        Feature feature = featureRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Funcionalidade não encontrada."));
        if (!feature.getCode().equals(request.code()) && featureRepository.existsByCode(request.code())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma funcionalidade com esse código.");
        }
        applyFeatureRequest(feature, request);
        return new FeatureDTO(featureRepository.save(feature));
    }

    private void applyPlanRequest(Plan plan, PlanRequest request) {
        plan.setCode(request.code());
        plan.setName(request.name());
        plan.setDescription(request.description());
        plan.setAmount(request.amount());
        plan.setCurrency("BRL");
        plan.setFrequency(request.frequency());
        plan.setFrequencyType(request.frequencyType());
        plan.setActive(request.active());
        plan.setMaxUsers(request.maxUsers());
        plan.setMaxCustomers(request.maxCustomers());
        plan.setMaxInventoryItems(request.maxInventoryItems());

        List<Long> ids = request.featureIds() == null ? List.of() : request.featureIds().stream().distinct().toList();
        List<Feature> features = featureRepository.findAllById(ids);
        if (features.size() != ids.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uma ou mais funcionalidades não existem.");
        }
        if (features.stream().anyMatch(feature -> !feature.isActive())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Planos não podem incluir funcionalidades inativas.");
        }
        plan.setFeatures(new LinkedHashSet<>(features));
    }

    private void applyFeatureRequest(Feature feature, FeatureRequest request) {
        feature.setCode(request.code());
        feature.setName(request.name());
        feature.setDescription(request.description());
        feature.setActive(request.active());
    }
}
