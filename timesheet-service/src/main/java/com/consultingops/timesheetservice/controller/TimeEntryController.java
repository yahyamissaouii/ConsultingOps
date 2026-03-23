package com.consultingops.timesheetservice.controller;

import com.consultingops.timesheetservice.dto.common.PageResponse;
import com.consultingops.timesheetservice.dto.timeentry.ApproveTimeEntryRequest;
import com.consultingops.timesheetservice.dto.timeentry.CreateTimeEntryRequest;
import com.consultingops.timesheetservice.dto.timeentry.RejectTimeEntryRequest;
import com.consultingops.timesheetservice.dto.timeentry.TimeEntryResponse;
import com.consultingops.timesheetservice.dto.timeentry.UpdateTimeEntryRequest;
import com.consultingops.timesheetservice.entity.enums.TimeEntryStatus;
import com.consultingops.timesheetservice.security.UserPrincipal;
import com.consultingops.timesheetservice.service.TimeEntryService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/time-entries")
@RequiredArgsConstructor
public class TimeEntryController {

    private final TimeEntryService timeEntryService;

    @Operation(summary = "Create a draft time entry")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TimeEntryResponse create(@Valid @RequestBody CreateTimeEntryRequest request,
                                    @AuthenticationPrincipal UserPrincipal principal) {
        return timeEntryService.create(request, principal);
    }

    @Operation(summary = "List time entries with pagination")
    @GetMapping
    public PageResponse<TimeEntryResponse> list(@RequestParam(required = false) UUID consultantId,
                                                @RequestParam(required = false) UUID projectId,
                                                @RequestParam(required = false) UUID clientId,
                                                @RequestParam(required = false) TimeEntryStatus status,
                                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "20") int size,
                                                @AuthenticationPrincipal UserPrincipal principal) {
        return timeEntryService.list(consultantId, projectId, clientId, status, startDate, endDate, page, size, principal);
    }

    @Operation(summary = "Get a time entry")
    @GetMapping("/{id}")
    public TimeEntryResponse get(@PathVariable UUID id, @AuthenticationPrincipal UserPrincipal principal) {
        return timeEntryService.get(id, principal);
    }

    @Operation(summary = "Update a draft or rejected time entry")
    @PutMapping("/{id}")
    public TimeEntryResponse update(@PathVariable UUID id,
                                    @Valid @RequestBody UpdateTimeEntryRequest request,
                                    @AuthenticationPrincipal UserPrincipal principal) {
        return timeEntryService.update(id, request, principal);
    }

    @Operation(summary = "Submit a time entry for approval")
    @PostMapping("/{id}/submit")
    public TimeEntryResponse submit(@PathVariable UUID id, @AuthenticationPrincipal UserPrincipal principal) {
        return timeEntryService.submit(id, principal);
    }

    @Operation(summary = "Approve a submitted time entry")
    @PostMapping("/{id}/approve")
    public TimeEntryResponse approve(@PathVariable UUID id,
                                     @RequestBody(required = false) ApproveTimeEntryRequest request,
                                     @AuthenticationPrincipal UserPrincipal principal) {
        return timeEntryService.approve(id, request == null ? new ApproveTimeEntryRequest(null) : request, principal);
    }

    @Operation(summary = "Reject a submitted time entry")
    @PostMapping("/{id}/reject")
    public TimeEntryResponse reject(@PathVariable UUID id,
                                    @Valid @RequestBody RejectTimeEntryRequest request,
                                    @AuthenticationPrincipal UserPrincipal principal) {
        return timeEntryService.reject(id, request, principal);
    }
}
