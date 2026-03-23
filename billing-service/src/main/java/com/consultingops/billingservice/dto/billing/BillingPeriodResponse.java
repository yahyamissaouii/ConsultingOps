package com.consultingops.billingservice.dto.billing;

import com.consultingops.billingservice.entity.enums.BillingPeriodStatus;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record BillingPeriodResponse(
        UUID id,
        UUID clientId,
        String clientName,
        LocalDate startDate,
        LocalDate endDate,
        BillingPeriodStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
