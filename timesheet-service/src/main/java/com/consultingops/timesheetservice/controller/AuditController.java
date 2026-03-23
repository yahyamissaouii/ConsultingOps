package com.consultingops.timesheetservice.controller;

import com.consultingops.timesheetservice.dto.audit.TimeEntryAuditResponse;
import com.consultingops.timesheetservice.dto.common.PageResponse;
import com.consultingops.timesheetservice.entity.enums.UserRole;
import com.consultingops.timesheetservice.security.UserPrincipal;
import com.consultingops.timesheetservice.service.TimeEntryAuditService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit/time-entries")
@RequiredArgsConstructor
public class AuditController {

    private final TimeEntryAuditService timeEntryAuditService;

    @Operation(summary = "List time entry audit records")
    @GetMapping
    public PageResponse<TimeEntryAuditResponse> list(@RequestParam(required = false) UUID timeEntryId,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size,
                                                     @AuthenticationPrincipal UserPrincipal principal) {
        if (principal.role() == UserRole.CONSULTANT) {
            throw new com.consultingops.timesheetservice.exception.UnauthorizedException("Consultants cannot access audit endpoints");
        }
        return timeEntryAuditService.list(timeEntryId, page, size);
    }
}
