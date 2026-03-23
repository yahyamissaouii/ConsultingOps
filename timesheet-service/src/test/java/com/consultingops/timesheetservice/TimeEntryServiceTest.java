package com.consultingops.timesheetservice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.consultingops.timesheetservice.dto.timeentry.ApproveTimeEntryRequest;
import com.consultingops.timesheetservice.entity.TimeEntry;
import com.consultingops.timesheetservice.entity.enums.TimeEntryStatus;
import com.consultingops.timesheetservice.entity.enums.UserRole;
import com.consultingops.timesheetservice.exception.BusinessRuleException;
import com.consultingops.timesheetservice.repository.TimeEntryRepository;
import com.consultingops.timesheetservice.security.UserPrincipal;
import com.consultingops.timesheetservice.service.TimeEntryAuditService;
import com.consultingops.timesheetservice.service.TimeEntryService;
import com.consultingops.timesheetservice.service.UserDirectoryService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TimeEntryServiceTest {

    @Mock
    private TimeEntryRepository timeEntryRepository;

    @Mock
    private UserDirectoryService userDirectoryService;

    @Mock
    private TimeEntryAuditService auditService;

    @InjectMocks
    private TimeEntryService timeEntryService;

    @Test
    void submitShouldTransitionDraftToSubmitted() {
        UUID entryId = UUID.randomUUID();
        UUID consultantId = UUID.randomUUID();
        TimeEntry entry = draftEntry(entryId, consultantId);
        UserPrincipal principal = new UserPrincipal(UUID.randomUUID(), "consultant@consultingops.local", UserRole.CONSULTANT, consultantId);

        when(timeEntryRepository.findById(entryId)).thenReturn(Optional.of(entry));
        when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = timeEntryService.submit(entryId, principal);

        assertThat(response.status()).isEqualTo(TimeEntryStatus.SUBMITTED);
        assertThat(response.submittedAt()).isNotNull();
        verify(auditService).record(eq(entryId), eq(principal.userId()), eq("TIME_ENTRY_SUBMITTED"), eq(TimeEntryStatus.DRAFT), eq(TimeEntryStatus.SUBMITTED), eq(null));
    }

    @Test
    void approveShouldRejectNonSubmittedEntries() {
        UUID entryId = UUID.randomUUID();
        TimeEntry entry = draftEntry(entryId, UUID.randomUUID());
        UserPrincipal manager = new UserPrincipal(UUID.randomUUID(), "manager@consultingops.local", UserRole.MANAGER, null);

        when(timeEntryRepository.findById(entryId)).thenReturn(Optional.of(entry));

        assertThatThrownBy(() -> timeEntryService.approve(entryId, new ApproveTimeEntryRequest("looks good"), manager))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("submitted entries");
    }

    private TimeEntry draftEntry(UUID id, UUID consultantId) {
        TimeEntry entry = new TimeEntry();
        entry.setId(id);
        entry.setConsultantId(consultantId);
        entry.setProjectId(UUID.randomUUID());
        entry.setClientId(UUID.randomUUID());
        entry.setConsultantNameSnapshot("Maya Patel");
        entry.setProjectNameSnapshot("ERP Modernization");
        entry.setClientNameSnapshot("Northwind Energy");
        entry.setHourlyRateSnapshot(new BigDecimal("120.00"));
        entry.setWorkDate(LocalDate.of(2026, 3, 20));
        entry.setHours(new BigDecimal("8.00"));
        entry.setDescription("Implementation");
        entry.setBillable(true);
        entry.setStatus(TimeEntryStatus.DRAFT);
        return entry;
    }
}
