package com.consultingops.userservice.dto.assignment;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record CreateProjectAssignmentRequest(
        @NotNull UUID consultantId,
        @NotNull UUID projectId,
        @NotBlank String assignedRole,
        @NotNull @Min(1) @Max(100) Integer allocationPercentage,
        @NotNull LocalDate startDate,
        LocalDate endDate,
        Boolean active
) {
}
