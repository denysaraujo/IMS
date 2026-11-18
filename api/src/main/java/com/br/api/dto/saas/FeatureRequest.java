package com.br.api.dto.saas;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record FeatureRequest(
        @NotBlank @Size(max = 80) @Pattern(regexp = "[a-z0-9][a-z0-9_-]*") String code,
        @NotBlank @Size(max = 255) String name,
        @Size(max = 1000) String description,
        boolean active
) {
}
