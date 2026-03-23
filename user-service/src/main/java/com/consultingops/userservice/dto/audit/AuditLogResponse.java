package com.consultingops.userservice.dto.audit;

import com.consultingops.userservice.entity.enums.AuditEntityType;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        UUID actorId,
        String action,
        AuditEntityType entityType,
        UUID entityId,
        String metadata,
        OffsetDateTime createdAt
) {
}
