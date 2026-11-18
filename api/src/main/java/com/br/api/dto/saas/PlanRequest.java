package com.br.api.dto.saas;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record PlanRequest(
        @NotBlank @Size(max = 80) @Pattern(regexp = "[a-z0-9][a-z0-9_-]*") String code,
        @NotBlank @Size(max = 255) String name,
        @Size(max = 2000) String description,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotNull @Positive Integer frequency,
        @NotBlank @Pattern(regexp = "months|days") String frequencyType,
        boolean active,
        @Min(1) Integer maxUsers,
        @Min(1) Integer maxCustomers,
        @Min(1) Integer maxInventoryItems,
        List<Long> featureIds
) {
}
