package com.consultingops.billingservice.dto.audit;

import java.time.OffsetDateTime;
import java.util.UUID;

public record BillingAuditResponse(
        UUID id,
        UUID actorId,
        String action,
        String entityType,
        UUID entityId,
        String metadata,
        OffsetDateTime createdAt
) {
}
