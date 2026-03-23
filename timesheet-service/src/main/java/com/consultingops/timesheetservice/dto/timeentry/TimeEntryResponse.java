package com.consultingops.timesheetservice.dto.timeentry;

import com.consultingops.timesheetservice.entity.enums.TimeEntryStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TimeEntryResponse(
        UUID id,
        UUID consultantId,
        String consultantName,
        UUID projectId,
        String projectName,
        UUID clientId,
        String clientName,
        LocalDate workDate,
        BigDecimal hours,
        String description,
        boolean billable,
        TimeEntryStatus status,
        OffsetDateTime submittedAt,
        OffsetDateTime approvedAt,
        UUID approvedBy,
        String rejectionReason,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
