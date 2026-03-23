package com.consultingops.userservice.dto.project;

import com.consultingops.userservice.entity.enums.BillingModel;
import com.consultingops.userservice.entity.enums.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record CreateProjectRequest(
        @NotBlank String code,
        @NotBlank String name,
        String description,
        @NotNull UUID clientId,
        @NotNull LocalDate startDate,
        LocalDate endDate,
        @NotNull BillingModel billingModel,
        @NotNull ProjectStatus status
) {
}
