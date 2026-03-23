package com.consultingops.timesheetservice.service;

import com.consultingops.timesheetservice.dto.audit.TimeEntryAuditResponse;
import com.consultingops.timesheetservice.dto.common.PageResponse;
import com.consultingops.timesheetservice.entity.TimeEntryAudit;
import com.consultingops.timesheetservice.entity.enums.TimeEntryStatus;
import com.consultingops.timesheetservice.repository.TimeEntryAuditRepository;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TimeEntryAuditService {

    private final TimeEntryAuditRepository timeEntryAuditRepository;

    public void record(UUID timeEntryId,
                       UUID actorId,
                       String action,
                       TimeEntryStatus oldStatus,
                       TimeEntryStatus newStatus,
                       String note) {
        TimeEntryAudit audit = new TimeEntryAudit();
        audit.setId(UUID.randomUUID());
        audit.setTimeEntryId(timeEntryId);
        audit.setActorId(actorId);
        audit.setAction(action);
        audit.setOldStatus(oldStatus);
        audit.setNewStatus(newStatus);
        audit.setNote(note);
        audit.setCreatedAt(OffsetDateTime.now());
        timeEntryAuditRepository.save(audit);
    }

    public PageResponse<TimeEntryAuditResponse> list(UUID timeEntryId, int page, int size) {
        var pageable = PageRequest.of(page, size);
        var result = timeEntryId == null
                ? timeEntryAuditRepository.findAll(pageable)
                : timeEntryAuditRepository.findByTimeEntryId(timeEntryId, pageable);
        return PageResponse.from(result.map(this::toResponse));
    }

    private TimeEntryAuditResponse toResponse(TimeEntryAudit audit) {
        return new TimeEntryAuditResponse(
                audit.getId(),
                audit.getTimeEntryId(),
                audit.getActorId(),
                audit.getAction(),
                audit.getOldStatus(),
                audit.getNewStatus(),
                audit.getNote(),
                audit.getCreatedAt()
        );
    }
}
