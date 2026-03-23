package com.consultingops.billingservice.service;

import com.consultingops.billingservice.client.ApprovedTimeEntryResponse;
import com.consultingops.billingservice.dto.billing.BillingGenerationResponse;
import com.consultingops.billingservice.dto.billing.BillingPeriodResponse;
import com.consultingops.billingservice.dto.billing.BillingSummaryResponse;
import com.consultingops.billingservice.dto.billing.GenerateBillingRequest;
import com.consultingops.billingservice.dto.common.PageResponse;
import com.consultingops.billingservice.entity.BillingPeriod;
import com.consultingops.billingservice.entity.BillingSummary;
import com.consultingops.billingservice.entity.enums.BillingPeriodStatus;
import com.consultingops.billingservice.entity.enums.UserRole;
import com.consultingops.billingservice.exception.BusinessRuleException;
import com.consultingops.billingservice.exception.ResourceNotFoundException;
import com.consultingops.billingservice.exception.UnauthorizedException;
import com.consultingops.billingservice.repository.BillingPeriodRepository;
import com.consultingops.billingservice.repository.BillingSummaryRepository;
import com.consultingops.billingservice.security.UserPrincipal;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BillingService {

    private final BillingPeriodRepository billingPeriodRepository;
    private final BillingSummaryRepository billingSummaryRepository;
    private final TimesheetAggregationService timesheetAggregationService;
    private final BillingAuditService billingAuditService;

    @Transactional
    public BillingGenerationResponse generate(GenerateBillingRequest request, UserPrincipal principal) {
        assertBillingPrivileges(principal);
        validatePeriod(request.startDate(), request.endDate());

        List<ApprovedTimeEntryResponse> approvedEntries = timesheetAggregationService
                .getApprovedEntries(request.clientId(), request.projectId(), request.startDate(), request.endDate())
                .stream()
                .filter(ApprovedTimeEntryResponse::billable)
                .toList();

        if (approvedEntries.isEmpty()) {
            throw new BusinessRuleException("No approved billable time entries found for the requested client and period");
        }

        ApprovedTimeEntryResponse first = approvedEntries.getFirst();
        var existingPeriod = billingPeriodRepository.findByClientIdAndStartDateAndEndDate(
                request.clientId(), request.startDate(), request.endDate());
        BillingPeriod period = existingPeriod
                .orElseGet(() -> {
                    BillingPeriod created = new BillingPeriod();
                    created.setId(UUID.randomUUID());
                    created.setClientId(request.clientId());
                    created.setClientNameSnapshot(first.clientName());
                    created.setStartDate(request.startDate());
                    created.setEndDate(request.endDate());
                    created.setStatus(BillingPeriodStatus.GENERATED);
                    return created;
                });

        period.setClientNameSnapshot(first.clientName());
        period.setStatus(BillingPeriodStatus.GENERATED);
        billingPeriodRepository.save(period);
        billingSummaryRepository.deleteByBillingPeriodId(period.getId());

        Map<String, List<ApprovedTimeEntryResponse>> groupedEntries = approvedEntries.stream()
                .collect(java.util.stream.Collectors.groupingBy(entry -> entry.projectId() + ":" + entry.consultantId()));

        List<BillingSummary> summaries = groupedEntries.values().stream()
                .map(group -> toSummary(period, request.currency(), group))
                .sorted(Comparator.comparing(BillingSummary::getProjectNameSnapshot).thenComparing(BillingSummary::getConsultantNameSnapshot))
                .toList();
        billingSummaryRepository.saveAll(summaries);

        BigDecimal totalAmount = summaries.stream()
                .map(BillingSummary::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String action = existingPeriod.isPresent() ? "BILLING_REGENERATED" : "BILLING_GENERATED";
        billingAuditService.record(principal.userId(), action, "BILLING_PERIOD", period.getId(),
                period.getClientId() + ":" + request.startDate() + ":" + request.endDate());

        return new BillingGenerationResponse(period.getId(), summaries.size(), totalAmount, toPeriodResponse(period));
    }

    public PageResponse<BillingSummaryResponse> listSummaries(UUID billingPeriodId,
                                                              UUID clientId,
                                                              UUID projectId,
                                                              UUID consultantId,
                                                              int page,
                                                              int size,
                                                              UserPrincipal principal) {
        assertReadPrivileges(principal);
        var result = billingSummaryRepository.findAll(
                buildSummarySpecification(billingPeriodId, clientId, projectId, consultantId),
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "clientNameSnapshot", "projectNameSnapshot", "consultantNameSnapshot")));
        return PageResponse.from(result.map(this::toSummaryResponse));
    }

    public BillingSummaryResponse getSummary(UUID id, UserPrincipal principal) {
        assertReadPrivileges(principal);
        BillingSummary summary = billingSummaryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Billing summary not found"));
        return toSummaryResponse(summary);
    }

    public PageResponse<BillingPeriodResponse> listPeriods(int page, int size, UserPrincipal principal) {
        assertReadPrivileges(principal);
        return PageResponse.from(billingPeriodRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startDate")))
                .map(this::toPeriodResponse));
    }

    private BillingSummary toSummary(BillingPeriod period, String currency, List<ApprovedTimeEntryResponse> entries) {
        ApprovedTimeEntryResponse first = entries.getFirst();
        BigDecimal approvedHours = entries.stream().map(ApprovedTimeEntryResponse::hours).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalAmount = approvedHours.multiply(first.hourlyRate()).setScale(2, RoundingMode.HALF_UP);

        BillingSummary summary = new BillingSummary();
        summary.setId(UUID.randomUUID());
        summary.setBillingPeriodId(period.getId());
        summary.setClientId(first.clientId());
        summary.setClientNameSnapshot(first.clientName());
        summary.setProjectId(first.projectId());
        summary.setProjectNameSnapshot(first.projectName());
        summary.setConsultantId(first.consultantId());
        summary.setConsultantNameSnapshot(first.consultantName());
        summary.setApprovedHours(approvedHours);
        summary.setHourlyRate(first.hourlyRate());
        summary.setTotalAmount(totalAmount);
        summary.setCurrency(currency);
        summary.setGeneratedAt(OffsetDateTime.now());
        return summary;
    }

    private Specification<BillingSummary> buildSummarySpecification(UUID billingPeriodId,
                                                                    UUID clientId,
                                                                    UUID projectId,
                                                                    UUID consultantId) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (billingPeriodId != null) {
                predicates.add(builder.equal(root.get("billingPeriodId"), billingPeriodId));
            }
            if (clientId != null) {
                predicates.add(builder.equal(root.get("clientId"), clientId));
            }
            if (projectId != null) {
                predicates.add(builder.equal(root.get("projectId"), projectId));
            }
            if (consultantId != null) {
                predicates.add(builder.equal(root.get("consultantId"), consultantId));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private void validatePeriod(java.time.LocalDate startDate, java.time.LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new BusinessRuleException("Billing period end date must be after start date");
        }
    }

    private void assertBillingPrivileges(UserPrincipal principal) {
        if (principal.role() != UserRole.ADMIN && principal.role() != UserRole.BILLING_ADMIN) {
            throw new UnauthorizedException("Only billing administrators or admins can generate billing");
        }
    }

    private void assertReadPrivileges(UserPrincipal principal) {
        if (principal.role() == UserRole.CONSULTANT) {
            throw new UnauthorizedException("Consultants cannot access billing data");
        }
    }

    private BillingSummaryResponse toSummaryResponse(BillingSummary summary) {
        return new BillingSummaryResponse(
                summary.getId(),
                summary.getBillingPeriodId(),
                summary.getClientId(),
                summary.getClientNameSnapshot(),
                summary.getProjectId(),
                summary.getProjectNameSnapshot(),
                summary.getConsultantId(),
                summary.getConsultantNameSnapshot(),
                summary.getApprovedHours(),
                summary.getHourlyRate(),
                summary.getTotalAmount(),
                summary.getCurrency(),
                summary.getGeneratedAt()
        );
    }

    private BillingPeriodResponse toPeriodResponse(BillingPeriod period) {
        return new BillingPeriodResponse(
                period.getId(),
                period.getClientId(),
                period.getClientNameSnapshot(),
                period.getStartDate(),
                period.getEndDate(),
                period.getStatus(),
                period.getCreatedAt(),
                period.getUpdatedAt()
        );
    }
}
