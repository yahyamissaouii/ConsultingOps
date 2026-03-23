package com.consultingops.timesheetservice.controller;

import com.consultingops.timesheetservice.dto.internal.ApprovedTimeEntryResponse;
import com.consultingops.timesheetservice.service.InternalAuthService;
import com.consultingops.timesheetservice.service.TimeEntryService;
import io.swagger.v3.oas.annotations.Hidden;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RestController
@RequestMapping("/internal/v1/time-entries")
@RequiredArgsConstructor
public class InternalTimesheetController {

    private final InternalAuthService internalAuthService;
    private final TimeEntryService timeEntryService;

    @GetMapping("/approved")
    public List<ApprovedTimeEntryResponse> approvedEntries(@RequestHeader("X-Internal-Api-Key") String apiKey,
                                                           @RequestParam(required = false) UUID clientId,
                                                           @RequestParam(required = false) UUID projectId,
                                                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        internalAuthService.assertValid(apiKey);
        return timeEntryService.getApprovedEntries(clientId, projectId, startDate, endDate);
    }
}
