package com.consultingops.billingservice.controller;

import com.consultingops.billingservice.dto.billing.BillingGenerationResponse;
import com.consultingops.billingservice.dto.billing.BillingSummaryResponse;
import com.consultingops.billingservice.dto.billing.GenerateBillingRequest;
import com.consultingops.billingservice.dto.common.PageResponse;
import com.consultingops.billingservice.security.UserPrincipal;
import com.consultingops.billingservice.service.BillingService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/v1/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    @Operation(summary = "Generate invoice-ready billing summaries from approved time entries")
    @PostMapping("/generate")
    public BillingGenerationResponse generate(@Valid @RequestBody GenerateBillingRequest request,
                                              @AuthenticationPrincipal UserPrincipal principal) {
        return billingService.generate(request, principal);
    }

    @Operation(summary = "List persisted billing summaries")
    @GetMapping("/summaries")
    public PageResponse<BillingSummaryResponse> listSummaries(@RequestParam(required = false) UUID billingPeriodId,
                                                              @RequestParam(required = false) UUID clientId,
                                                              @RequestParam(required = false) UUID projectId,
                                                              @RequestParam(required = false) UUID consultantId,
                                                              @RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "20") int size,
                                                              @AuthenticationPrincipal UserPrincipal principal) {
        return billingService.listSummaries(billingPeriodId, clientId, projectId, consultantId, page, size, principal);
    }

    @Operation(summary = "Get a single billing summary")
    @GetMapping("/summaries/{id}")
    public BillingSummaryResponse getSummary(@PathVariable UUID id, @AuthenticationPrincipal UserPrincipal principal) {
        return billingService.getSummary(id, principal);
    }
}
