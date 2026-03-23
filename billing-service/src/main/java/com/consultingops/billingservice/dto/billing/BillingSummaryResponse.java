package com.consultingops.billingservice.dto.billing;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record BillingSummaryResponse(
        UUID id,
        UUID billingPeriodId,
        UUID clientId,
        String clientName,
        UUID projectId,
        String projectName,
        UUID consultantId,
        String consultantName,
        BigDecimal approvedHours,
        BigDecimal hourlyRate,
        BigDecimal totalAmount,
        String currency,
        OffsetDateTime generatedAt
) {
}
