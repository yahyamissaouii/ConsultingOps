package com.consultingops.timesheetservice.dto.audit;

import com.consultingops.timesheetservice.entity.enums.TimeEntryStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TimeEntryAuditResponse(
        UUID id,
        UUID timeEntryId,
        UUID actorId,
        String action,
        TimeEntryStatus oldStatus,
        TimeEntryStatus newStatus,
        String note,
        OffsetDateTime createdAt
) {
}
