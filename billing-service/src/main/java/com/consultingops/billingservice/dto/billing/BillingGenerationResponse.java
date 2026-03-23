package com.consultingops.billingservice.dto.billing;

import java.math.BigDecimal;
import java.util.UUID;

public record BillingGenerationResponse(
        UUID billingPeriodId,
        int summaryCount,
        BigDecimal totalAmount,
        BillingPeriodResponse billingPeriod
) {
}
