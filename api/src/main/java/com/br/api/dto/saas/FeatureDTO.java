package com.br.api.dto.saas;

import com.br.api.model.saas.Feature;

public record FeatureDTO(Long id, String code, String name, String description, boolean active) {
    public FeatureDTO(Feature feature) {
        this(feature.getId(), feature.getCode(), feature.getName(), feature.getDescription(), feature.isActive());
    }
}
