package com.consultingops.timesheetservice.dto.timeentry;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateTimeEntryRequest(
        @NotNull UUID consultantId,
        @NotNull UUID projectId,
        @NotNull LocalDate workDate,
        @NotNull @DecimalMin("0.25") @DecimalMax("24.0") BigDecimal hours,
        @NotBlank String description,
        @NotNull Boolean billable
) {
}
