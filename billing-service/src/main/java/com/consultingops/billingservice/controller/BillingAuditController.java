package com.consultingops.billingservice.controller;

import com.consultingops.billingservice.dto.audit.BillingAuditResponse;
import com.consultingops.billingservice.dto.common.PageResponse;
import com.consultingops.billingservice.entity.enums.UserRole;
import com.consultingops.billingservice.security.UserPrincipal;
import com.consultingops.billingservice.service.BillingAuditService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit/billing")
@RequiredArgsConstructor
public class BillingAuditController {

    private final BillingAuditService billingAuditService;

    @Operation(summary = "List billing audit events")
    @GetMapping
    public PageResponse<BillingAuditResponse> list(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size,
                                                   @AuthenticationPrincipal UserPrincipal principal) {
        if (principal.role() == UserRole.CONSULTANT) {
            throw new com.consultingops.billingservice.exception.UnauthorizedException("Consultants cannot access billing audit events");
        }
        return billingAuditService.list(page, size);
    }
}
