package com.consultingops.userservice.dto.assignment;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ProjectAssignmentResponse(
        UUID id,
        UUID consultantId,
        String consultantName,
        UUID projectId,
        String projectName,
        String assignedRole,
        Integer allocationPercentage,
        LocalDate startDate,
        LocalDate endDate,
        boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
