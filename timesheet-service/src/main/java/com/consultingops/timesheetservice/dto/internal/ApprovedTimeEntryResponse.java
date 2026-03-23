package com.consultingops.timesheetservice.dto.internal;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ApprovedTimeEntryResponse(
        UUID timeEntryId,
        UUID consultantId,
        String consultantName,
        UUID projectId,
        String projectName,
        UUID clientId,
        String clientName,
        LocalDate workDate,
        BigDecimal hours,
        BigDecimal hourlyRate,
        boolean billable
) {
}
