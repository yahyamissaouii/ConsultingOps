package com.consultingops.timesheetservice.service;

import com.consultingops.timesheetservice.client.AssignmentValidationResponse;
import com.consultingops.timesheetservice.dto.common.PageResponse;
import com.consultingops.timesheetservice.dto.internal.ApprovedTimeEntryResponse;
import com.consultingops.timesheetservice.dto.report.TimeEntryReportResponse;
import com.consultingops.timesheetservice.dto.timeentry.ApproveTimeEntryRequest;
import com.consultingops.timesheetservice.dto.timeentry.CreateTimeEntryRequest;
import com.consultingops.timesheetservice.dto.timeentry.RejectTimeEntryRequest;
import com.consultingops.timesheetservice.dto.timeentry.TimeEntryResponse;
import com.consultingops.timesheetservice.dto.timeentry.UpdateTimeEntryRequest;
import com.consultingops.timesheetservice.entity.TimeEntry;
import com.consultingops.timesheetservice.entity.enums.TimeEntryStatus;
import com.consultingops.timesheetservice.entity.enums.UserRole;
import com.consultingops.timesheetservice.exception.BusinessRuleException;
import com.consultingops.timesheetservice.exception.ResourceNotFoundException;
import com.consultingops.timesheetservice.exception.UnauthorizedException;
import com.consultingops.timesheetservice.repository.TimeEntryRepository;
import com.consultingops.timesheetservice.security.UserPrincipal;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TimeEntryService {

    private final TimeEntryRepository timeEntryRepository;
    private final UserDirectoryService userDirectoryService;
    private final TimeEntryAuditService auditService;

    public TimeEntryResponse create(CreateTimeEntryRequest request, UserPrincipal principal) {
        assertConsultantOwnership(principal, request.consultantId());
        AssignmentValidationResponse assignment = userDirectoryService.validateAssignment(
                request.consultantId(), request.projectId(), request.workDate());

        TimeEntry entry = new TimeEntry();
        entry.setId(UUID.randomUUID());
        entry.setConsultantId(request.consultantId());
        entry.setProjectId(request.projectId());
        entry.setClientId(assignment.clientId());
        entry.setConsultantNameSnapshot(assignment.consultantName());
        entry.setProjectNameSnapshot(assignment.projectName());
        entry.setClientNameSnapshot(assignment.clientName());
        entry.setHourlyRateSnapshot(assignment.hourlyRate());
        entry.setWorkDate(request.workDate());
        entry.setHours(request.hours());
        entry.setDescription(request.description());
        entry.setBillable(request.billable());
        entry.setStatus(TimeEntryStatus.DRAFT);
        timeEntryRepository.save(entry);

        auditService.record(entry.getId(), principal.userId(), "TIME_ENTRY_CREATED", null, TimeEntryStatus.DRAFT, null);
        return toResponse(entry);
    }

    public TimeEntryResponse update(UUID id, UpdateTimeEntryRequest request, UserPrincipal principal) {
        TimeEntry entry = findEntity(id);
        ensureCanMutate(entry, principal);
        ensureEditable(entry);

        AssignmentValidationResponse assignment = userDirectoryService.validateAssignment(
                entry.getConsultantId(), request.projectId(), request.workDate());

        entry.setProjectId(request.projectId());
        entry.setClientId(assignment.clientId());
        entry.setConsultantNameSnapshot(assignment.consultantName());
        entry.setProjectNameSnapshot(assignment.projectName());
        entry.setClientNameSnapshot(assignment.clientName());
        entry.setHourlyRateSnapshot(assignment.hourlyRate());
        entry.setWorkDate(request.workDate());
        entry.setHours(request.hours());
        entry.setDescription(request.description());
        entry.setBillable(request.billable());
        timeEntryRepository.save(entry);

        auditService.record(entry.getId(), principal.userId(), "TIME_ENTRY_UPDATED", entry.getStatus(), entry.getStatus(), null);
        return toResponse(entry);
    }

    public TimeEntryResponse get(UUID id, UserPrincipal principal) {
        TimeEntry entry = findEntity(id);
        ensureCanView(entry, principal);
        return toResponse(entry);
    }

    public PageResponse<TimeEntryResponse> list(UUID consultantId,
                                                UUID projectId,
                                                UUID clientId,
                                                TimeEntryStatus status,
                                                LocalDate startDate,
                                                LocalDate endDate,
                                                int page,
                                                int size,
                                                UserPrincipal principal) {
        UUID effectiveConsultantId = principal.role() == UserRole.CONSULTANT ? principal.consultantId() : consultantId;
        var result = timeEntryRepository.findAll(buildSpecification(effectiveConsultantId, projectId, clientId, status, startDate, endDate),
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "workDate", "createdAt")));
        return PageResponse.from(result.map(this::toResponse));
    }

    public TimeEntryReportResponse report(UUID consultantId,
                                          UUID projectId,
                                          UUID clientId,
                                          TimeEntryStatus status,
                                          LocalDate startDate,
                                          LocalDate endDate,
                                          UserPrincipal principal) {
        UUID effectiveConsultantId = principal.role() == UserRole.CONSULTANT ? principal.consultantId() : consultantId;
        List<TimeEntryResponse> entries = timeEntryRepository.findAll(
                        buildSpecification(effectiveConsultantId, projectId, clientId, status, startDate, endDate),
                        Sort.by(Sort.Direction.DESC, "workDate", "createdAt"))
                .stream()
                .map(this::toResponse)
                .toList();

        BigDecimal totalHours = entries.stream().map(TimeEntryResponse::hours).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal billableHours = entries.stream()
                .filter(TimeEntryResponse::billable)
                .map(TimeEntryResponse::hours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new TimeEntryReportResponse(totalHours, billableHours, entries);
    }

    public TimeEntryResponse submit(UUID id, UserPrincipal principal) {
        TimeEntry entry = findEntity(id);
        ensureCanMutate(entry, principal);
        if (entry.getStatus() != TimeEntryStatus.DRAFT && entry.getStatus() != TimeEntryStatus.REJECTED) {
            throw new BusinessRuleException("Only draft or rejected entries can be submitted");
        }

        TimeEntryStatus oldStatus = entry.getStatus();
        entry.setStatus(TimeEntryStatus.SUBMITTED);
        entry.setSubmittedAt(OffsetDateTime.now());
        entry.setApprovedAt(null);
        entry.setApprovedBy(null);
        entry.setRejectionReason(null);
        timeEntryRepository.save(entry);

        auditService.record(entry.getId(), principal.userId(), "TIME_ENTRY_SUBMITTED", oldStatus, TimeEntryStatus.SUBMITTED, null);
        return toResponse(entry);
    }

    public TimeEntryResponse approve(UUID id, ApproveTimeEntryRequest request, UserPrincipal principal) {
        ensureManagerPrivileges(principal);
        TimeEntry entry = findEntity(id);
        if (entry.getStatus() != TimeEntryStatus.SUBMITTED) {
            throw new BusinessRuleException("Only submitted entries can be approved");
        }

        entry.setStatus(TimeEntryStatus.APPROVED);
        entry.setApprovedAt(OffsetDateTime.now());
        entry.setApprovedBy(principal.userId());
        entry.setRejectionReason(null);
        timeEntryRepository.save(entry);

        auditService.record(entry.getId(), principal.userId(), "TIME_ENTRY_APPROVED", TimeEntryStatus.SUBMITTED, TimeEntryStatus.APPROVED, request.note());
        return toResponse(entry);
    }

    public TimeEntryResponse reject(UUID id, RejectTimeEntryRequest request, UserPrincipal principal) {
        ensureManagerPrivileges(principal);
        TimeEntry entry = findEntity(id);
        if (entry.getStatus() != TimeEntryStatus.SUBMITTED) {
            throw new BusinessRuleException("Only submitted entries can be rejected");
        }

        entry.setStatus(TimeEntryStatus.REJECTED);
        entry.setApprovedAt(null);
        entry.setApprovedBy(null);
        entry.setRejectionReason(request.reason());
        timeEntryRepository.save(entry);

        auditService.record(entry.getId(), principal.userId(), "TIME_ENTRY_REJECTED", TimeEntryStatus.SUBMITTED, TimeEntryStatus.REJECTED, request.reason());
        return toResponse(entry);
    }

    public List<ApprovedTimeEntryResponse> getApprovedEntries(UUID clientId, UUID projectId, LocalDate startDate, LocalDate endDate) {
        return timeEntryRepository.findAll(buildApprovedSpecification(clientId, projectId, startDate, endDate))
                .stream()
                .map(entry -> new ApprovedTimeEntryResponse(
                        entry.getId(),
                        entry.getConsultantId(),
                        entry.getConsultantNameSnapshot(),
                        entry.getProjectId(),
                        entry.getProjectNameSnapshot(),
                        entry.getClientId(),
                        entry.getClientNameSnapshot(),
                        entry.getWorkDate(),
                        entry.getHours(),
                        entry.getHourlyRateSnapshot(),
                        entry.isBillable()
                ))
                .toList();
    }

    public TimeEntry findEntity(UUID id) {
        return timeEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Time entry not found"));
    }

    private void ensureCanMutate(TimeEntry entry, UserPrincipal principal) {
        if (principal.role() == UserRole.CONSULTANT && !entry.getConsultantId().equals(principal.consultantId())) {
            throw new UnauthorizedException("Consultants can only modify their own time entries");
        }
    }

    private void ensureCanView(TimeEntry entry, UserPrincipal principal) {
        if (principal.role() == UserRole.CONSULTANT && !entry.getConsultantId().equals(principal.consultantId())) {
            throw new UnauthorizedException("Consultants can only access their own time entries");
        }
    }

    private void ensureEditable(TimeEntry entry) {
        if (entry.getStatus() != TimeEntryStatus.DRAFT && entry.getStatus() != TimeEntryStatus.REJECTED) {
            throw new BusinessRuleException("Only draft or rejected entries can be edited");
        }
    }

    private void ensureManagerPrivileges(UserPrincipal principal) {
        if (principal.role() != UserRole.ADMIN && principal.role() != UserRole.MANAGER) {
            throw new UnauthorizedException("Only admins or managers can approve or reject entries");
        }
    }

    private void assertConsultantOwnership(UserPrincipal principal, UUID consultantId) {
        if (principal.role() == UserRole.CONSULTANT && !consultantId.equals(principal.consultantId())) {
            throw new UnauthorizedException("Consultants can only create entries for themselves");
        }
    }

    private Specification<TimeEntry> buildSpecification(UUID consultantId,
                                                        UUID projectId,
                                                        UUID clientId,
                                                        TimeEntryStatus status,
                                                        LocalDate startDate,
                                                        LocalDate endDate) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (consultantId != null) {
                predicates.add(builder.equal(root.get("consultantId"), consultantId));
            }
            if (projectId != null) {
                predicates.add(builder.equal(root.get("projectId"), projectId));
            }
            if (clientId != null) {
                predicates.add(builder.equal(root.get("clientId"), clientId));
            }
            if (status != null) {
                predicates.add(builder.equal(root.get("status"), status));
            }
            if (startDate != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("workDate"), startDate));
            }
            if (endDate != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("workDate"), endDate));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Specification<TimeEntry> buildApprovedSpecification(UUID clientId, UUID projectId, LocalDate startDate, LocalDate endDate) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(builder.equal(root.get("status"), TimeEntryStatus.APPROVED));
            if (clientId != null) {
                predicates.add(builder.equal(root.get("clientId"), clientId));
            }
            if (projectId != null) {
                predicates.add(builder.equal(root.get("projectId"), projectId));
            }
            if (startDate != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("workDate"), startDate));
            }
            if (endDate != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("workDate"), endDate));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private TimeEntryResponse toResponse(TimeEntry entry) {
        return new TimeEntryResponse(
                entry.getId(),
                entry.getConsultantId(),
                entry.getConsultantNameSnapshot(),
                entry.getProjectId(),
                entry.getProjectNameSnapshot(),
                entry.getClientId(),
                entry.getClientNameSnapshot(),
                entry.getWorkDate(),
                entry.getHours(),
                entry.getDescription(),
                entry.isBillable(),
                entry.getStatus(),
                entry.getSubmittedAt(),
                entry.getApprovedAt(),
                entry.getApprovedBy(),
                entry.getRejectionReason(),
                entry.getCreatedAt(),
                entry.getUpdatedAt()
        );
    }
}
