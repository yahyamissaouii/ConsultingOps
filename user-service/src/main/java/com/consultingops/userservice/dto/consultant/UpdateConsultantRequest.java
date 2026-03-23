package com.consultingops.userservice.dto.consultant;

import com.consultingops.userservice.entity.enums.ConsultantStatus;
import com.consultingops.userservice.entity.enums.SeniorityLevel;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record UpdateConsultantRequest(
        @NotBlank String fullName,
        @NotBlank String jobTitle,
        @NotNull SeniorityLevel seniorityLevel,
        @NotNull @DecimalMin("0.0") BigDecimal hourlyRate,
        @NotNull ConsultantStatus status,
        @NotNull Boolean active
) {
}
