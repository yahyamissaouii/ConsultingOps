package com.consultingops.billingservice.dto.billing;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record GenerateBillingRequest(
        @NotNull UUID clientId,
        UUID projectId,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @NotBlank String currency
) {
}
