package com.consultingops.timesheetservice.dto.report;

import com.consultingops.timesheetservice.dto.timeentry.TimeEntryResponse;
import java.math.BigDecimal;
import java.util.List;

public record TimeEntryReportResponse(
        BigDecimal totalHours,
        BigDecimal billableHours,
        List<TimeEntryResponse> entries
) {
}
