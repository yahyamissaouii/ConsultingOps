package com.consultingops.userservice.controller;

import com.consultingops.userservice.dto.audit.AuditLogResponse;
import com.consultingops.userservice.dto.common.PageResponse;
import com.consultingops.userservice.entity.enums.AuditEntityType;
import com.consultingops.userservice.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @Operation(summary = "List audit log entries for privileged users")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','BILLING_ADMIN')")
    public PageResponse<AuditLogResponse> list(@RequestParam(required = false) AuditEntityType entityType,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        return auditService.list(entityType, page, size);
    }
}
