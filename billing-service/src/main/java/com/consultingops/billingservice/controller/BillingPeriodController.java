package com.consultingops.billingservice.controller;

import com.consultingops.billingservice.dto.billing.BillingPeriodResponse;
import com.consultingops.billingservice.dto.common.PageResponse;
import com.consultingops.billingservice.security.UserPrincipal;
import com.consultingops.billingservice.service.BillingService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/billing-periods")
@RequiredArgsConstructor
public class BillingPeriodController {

    private final BillingService billingService;

    @Operation(summary = "List billing periods")
    @GetMapping
    public PageResponse<BillingPeriodResponse> list(@RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "20") int size,
                                                    @AuthenticationPrincipal UserPrincipal principal) {
        return billingService.listPeriods(page, size, principal);
    }
}
