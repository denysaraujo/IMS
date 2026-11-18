package com.br.api.dto.saas;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Positive;

public record SignupRequest(
        @NotBlank @Size(max = 255) String companyName,
        @NotBlank @Size(max = 255) String ownerName,
        @Size(max = 32) String companyDocument,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 3, max = 80) String username,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotNull @Positive Long planId
) {
}
