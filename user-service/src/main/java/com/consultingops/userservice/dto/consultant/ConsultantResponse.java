package com.consultingops.userservice.dto.consultant;

import com.consultingops.userservice.entity.enums.ConsultantStatus;
import com.consultingops.userservice.entity.enums.SeniorityLevel;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ConsultantResponse(
        UUID id,
        UUID userId,
        String fullName,
        String email,
        String employeeCode,
        String jobTitle,
        SeniorityLevel seniorityLevel,
        BigDecimal hourlyRate,
        ConsultantStatus status,
        boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
