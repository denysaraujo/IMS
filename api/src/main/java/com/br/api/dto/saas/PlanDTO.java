package com.br.api.dto.saas;

import com.br.api.model.saas.Plan;

import java.math.BigDecimal;
import java.util.List;

public record PlanDTO(
        Long id,
        String code,
        String name,
        String description,
        BigDecimal amount,
        String currency,
        Integer frequency,
        String frequencyType,
        boolean active,
        Integer maxUsers,
        Integer maxCustomers,
        Integer maxInventoryItems,
        List<FeatureDTO> features
) {
    public PlanDTO(Plan plan) {
        this(
                plan.getId(),
                plan.getCode(),
                plan.getName(),
                plan.getDescription(),
                plan.getAmount(),
                plan.getCurrency(),
                plan.getFrequency(),
                plan.getFrequencyType(),
                plan.isActive(),
                plan.getMaxUsers(),
                plan.getMaxCustomers(),
                plan.getMaxInventoryItems(),
                plan.getFeatures().stream().map(FeatureDTO::new).toList()
        );
    }
}
