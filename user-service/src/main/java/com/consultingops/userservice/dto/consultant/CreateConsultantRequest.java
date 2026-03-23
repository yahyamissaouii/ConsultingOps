package com.consultingops.userservice.dto.consultant;

import com.consultingops.userservice.entity.enums.ConsultantStatus;
import com.consultingops.userservice.entity.enums.SeniorityLevel;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreateConsultantRequest(
        @NotBlank String fullName,
        @Email @NotBlank String email,
        @NotBlank @Size(min = 8, max = 128) String password,
        @NotBlank String employeeCode,
        @NotBlank String jobTitle,
        @NotNull SeniorityLevel seniorityLevel,
        @NotNull @DecimalMin("0.0") BigDecimal hourlyRate,
        @NotNull ConsultantStatus status
) {
}
