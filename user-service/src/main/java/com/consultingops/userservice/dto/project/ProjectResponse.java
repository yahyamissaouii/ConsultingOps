package com.consultingops.userservice.dto.project;

import com.consultingops.userservice.entity.enums.BillingModel;
import com.consultingops.userservice.entity.enums.ProjectStatus;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        String code,
        String name,
        String description,
        UUID clientId,
        String clientName,
        LocalDate startDate,
        LocalDate endDate,
        BillingModel billingModel,
        ProjectStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
