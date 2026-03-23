package com.consultingops.timesheetservice.controller;

import com.consultingops.timesheetservice.dto.report.TimeEntryReportResponse;
import com.consultingops.timesheetservice.entity.enums.TimeEntryStatus;
import com.consultingops.timesheetservice.security.UserPrincipal;
import com.consultingops.timesheetservice.service.TimeEntryService;
import io.swagger.v3.oas.annotations.Operation;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final TimeEntryService timeEntryService;

    @Operation(summary = "Report time entries by consultant, project, client, date range, and status")
    @GetMapping("/time-entries")
    public TimeEntryReportResponse report(@RequestParam(required = false) UUID consultantId,
                                          @RequestParam(required = false) UUID projectId,
                                          @RequestParam(required = false) UUID clientId,
                                          @RequestParam(required = false) TimeEntryStatus status,
                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                          @AuthenticationPrincipal UserPrincipal principal) {
        return timeEntryService.report(consultantId, projectId, clientId, status, startDate, endDate, principal);
    }
}
