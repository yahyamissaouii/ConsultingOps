package com.consultingops.billingservice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.consultingops.billingservice.client.ApprovedTimeEntryResponse;
import com.consultingops.billingservice.dto.billing.GenerateBillingRequest;
import com.consultingops.billingservice.entity.BillingPeriod;
import com.consultingops.billingservice.entity.BillingSummary;
import com.consultingops.billingservice.entity.enums.BillingPeriodStatus;
import com.consultingops.billingservice.entity.enums.UserRole;
import com.consultingops.billingservice.repository.BillingPeriodRepository;
import com.consultingops.billingservice.repository.BillingSummaryRepository;
import com.consultingops.billingservice.security.UserPrincipal;
import com.consultingops.billingservice.service.BillingAuditService;
import com.consultingops.billingservice.service.BillingService;
import com.consultingops.billingservice.service.TimesheetAggregationService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BillingServiceTest {

    @Mock
    private BillingPeriodRepository billingPeriodRepository;

    @Mock
    private BillingSummaryRepository billingSummaryRepository;

    @Mock
    private TimesheetAggregationService timesheetAggregationService;

    @Mock
    private BillingAuditService billingAuditService;

    @InjectMocks
    private BillingService billingService;

    @Test
    void generateShouldAggregateApprovedBillableEntries() {
        UUID clientId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID consultantId = UUID.randomUUID();
        GenerateBillingRequest request = new GenerateBillingRequest(clientId, projectId, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31), "EUR");
        UserPrincipal principal = new UserPrincipal(UUID.randomUUID(), "billing@consultingops.local", UserRole.BILLING_ADMIN, null);

        when(timesheetAggregationService.getApprovedEntries(clientId, projectId, request.startDate(), request.endDate()))
                .thenReturn(List.of(
                        approvedEntry(clientId, projectId, consultantId, new BigDecimal("6.00")),
                        approvedEntry(clientId, projectId, consultantId, new BigDecimal("2.00"))
                ));
        when(billingPeriodRepository.findByClientIdAndStartDateAndEndDate(clientId, request.startDate(), request.endDate()))
                .thenReturn(Optional.empty());
        when(billingPeriodRepository.save(any(BillingPeriod.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(billingSummaryRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = billingService.generate(request, principal);

        assertThat(response.summaryCount()).isEqualTo(1);
        assertThat(response.totalAmount()).isEqualByComparingTo("800.00");
        assertThat(response.billingPeriod().status()).isEqualTo(BillingPeriodStatus.GENERATED);

        ArgumentCaptor<List<BillingSummary>> summaryCaptor = ArgumentCaptor.forClass(List.class);
        verify(billingSummaryRepository).saveAll(summaryCaptor.capture());
        assertThat(summaryCaptor.getValue()).hasSize(1);
        assertThat(summaryCaptor.getValue().getFirst().getApprovedHours()).isEqualByComparingTo("8.00");
        verify(billingAuditService).record(eq(principal.userId()), eq("BILLING_GENERATED"), eq("BILLING_PERIOD"), any(), any());
    }

    private ApprovedTimeEntryResponse approvedEntry(UUID clientId, UUID projectId, UUID consultantId, BigDecimal hours) {
        return new ApprovedTimeEntryResponse(
                UUID.randomUUID(),
                consultantId,
                "Maya Patel",
                projectId,
                "ERP Modernization",
                clientId,
                "Northwind Energy",
                LocalDate.of(2026, 3, 20),
                hours,
                new BigDecimal("100.00"),
                true
        );
    }
}
