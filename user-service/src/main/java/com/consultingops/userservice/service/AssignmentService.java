package com.consultingops.userservice.service;

import com.consultingops.userservice.dto.assignment.CreateProjectAssignmentRequest;
import com.consultingops.userservice.dto.assignment.ProjectAssignmentResponse;
import com.consultingops.userservice.dto.common.PageResponse;
import com.consultingops.userservice.dto.internal.AssignmentValidationResponse;
import com.consultingops.userservice.entity.Client;
import com.consultingops.userservice.entity.Consultant;
import com.consultingops.userservice.entity.Project;
import com.consultingops.userservice.entity.ProjectAssignment;
import com.consultingops.userservice.entity.User;
import com.consultingops.userservice.entity.enums.AuditEntityType;
import com.consultingops.userservice.entity.enums.ConsultantStatus;
import com.consultingops.userservice.entity.enums.ProjectStatus;
import com.consultingops.userservice.exception.BusinessRuleException;
import com.consultingops.userservice.exception.ResourceNotFoundException;
import com.consultingops.userservice.repository.ProjectAssignmentRepository;
import com.consultingops.userservice.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final ProjectAssignmentRepository assignmentRepository;
    private final ConsultantService consultantService;
    private final ProjectService projectService;
    private final ClientService clientService;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public ProjectAssignmentResponse create(CreateProjectAssignmentRequest request, UUID actorId) {
        validateAssignmentDates(request.startDate(), request.endDate());

        Consultant consultant = consultantService.findEntity(request.consultantId());
        if (consultant.getStatus() != ConsultantStatus.ACTIVE) {
            throw new BusinessRuleException("Only active consultants can be assigned");
        }

        Project project = projectService.findEntity(request.projectId());
        if (project.getStatus() != ProjectStatus.ACTIVE && project.getStatus() != ProjectStatus.PLANNED) {
            throw new BusinessRuleException("Assignments can only target planned or active projects");
        }

        if (request.startDate().isBefore(project.getStartDate())
                || (project.getEndDate() != null && request.endDate() != null && request.endDate().isAfter(project.getEndDate()))) {
            throw new BusinessRuleException("Assignment dates must be within the project schedule");
        }

        ensureNoOverallocation(consultant.getId(), request.startDate(), request.endDate(), request.allocationPercentage());

        ProjectAssignment assignment = new ProjectAssignment();
        assignment.setId(UUID.randomUUID());
        assignment.setConsultantId(request.consultantId());
        assignment.setProjectId(request.projectId());
        assignment.setAssignedRole(request.assignedRole());
        assignment.setAllocationPercentage(request.allocationPercentage());
        assignment.setStartDate(request.startDate());
        assignment.setEndDate(request.endDate());
        assignment.setActive(request.active() == null || request.active());
        assignmentRepository.save(assignment);

        auditService.record(actorId, "PROJECT_ASSIGNMENT_CREATED", AuditEntityType.PROJECT_ASSIGNMENT, assignment.getId(),
                assignment.getConsultantId() + ":" + assignment.getProjectId());
        return toResponse(assignment, consultant, project);
    }

    public ProjectAssignmentResponse get(UUID id) {
        ProjectAssignment assignment = findEntity(id);
        return toResponse(assignment, consultantService.findEntity(assignment.getConsultantId()), projectService.findEntity(assignment.getProjectId()));
    }

    public PageResponse<ProjectAssignmentResponse> list(UUID consultantId, UUID projectId, int page, int size) {
        var pageable = PageRequest.of(page, size);
        var result = consultantId != null
                ? assignmentRepository.findByConsultantId(consultantId, pageable)
                : projectId != null ? assignmentRepository.findByProjectId(projectId, pageable) : assignmentRepository.findAll(pageable);
        return PageResponse.from(result.map(assignment ->
                toResponse(assignment, consultantService.findEntity(assignment.getConsultantId()), projectService.findEntity(assignment.getProjectId()))));
    }

    public AssignmentValidationResponse validateAssignment(UUID consultantId, UUID projectId, LocalDate workDate) {
        Consultant consultant = consultantService.findEntity(consultantId);
        Project project = projectService.findEntity(projectId);
        Client client = clientService.findEntity(project.getClientId());
        User user = userRepository.findById(consultant.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Linked consultant user not found"));

        ProjectAssignment assignment = assignmentRepository.findByConsultantIdAndActiveTrue(consultantId)
                .stream()
                .filter(candidate -> candidate.getProjectId().equals(projectId))
                .filter(candidate -> overlaps(workDate, workDate, candidate.getStartDate(), candidate.getEndDate()))
                .findFirst()
                .orElseThrow(() -> new BusinessRuleException("No active assignment found for consultant/project/date"));

        return new AssignmentValidationResponse(
                assignment.getId(),
                consultant.getId(),
                user.getFullName(),
                project.getId(),
                project.getName(),
                client.getId(),
                client.getName(),
                consultant.getHourlyRate()
        );
    }

    public ProjectAssignment findEntity(UUID id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project assignment not found"));
    }

    private void validateAssignmentDates(LocalDate startDate, LocalDate endDate) {
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new BusinessRuleException("Assignment end date must be after start date");
        }
    }

    private void ensureNoOverallocation(UUID consultantId, LocalDate startDate, LocalDate endDate, int requestedAllocation) {
        List<ProjectAssignment> activeAssignments = assignmentRepository.findByConsultantIdAndActiveTrue(consultantId);
        int overlappingAllocation = activeAssignments.stream()
                .filter(assignment -> overlaps(startDate, endDate, assignment.getStartDate(), assignment.getEndDate()))
                .mapToInt(ProjectAssignment::getAllocationPercentage)
                .sum();

        if (overlappingAllocation + requestedAllocation > 100) {
            throw new BusinessRuleException("Assignment exceeds 100% allocation for overlapping dates");
        }
    }

    private boolean overlaps(LocalDate startOne, LocalDate endOne, LocalDate startTwo, LocalDate endTwo) {
        LocalDate effectiveEndOne = endOne == null ? LocalDate.MAX : endOne;
        LocalDate effectiveEndTwo = endTwo == null ? LocalDate.MAX : endTwo;
        return !startOne.isAfter(effectiveEndTwo) && !startTwo.isAfter(effectiveEndOne);
    }

    private ProjectAssignmentResponse toResponse(ProjectAssignment assignment, Consultant consultant, Project project) {
        User user = userRepository.findById(consultant.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Linked consultant user not found"));
        return new ProjectAssignmentResponse(
                assignment.getId(),
                assignment.getConsultantId(),
                user.getFullName(),
                assignment.getProjectId(),
                project.getName(),
                assignment.getAssignedRole(),
                assignment.getAllocationPercentage(),
                assignment.getStartDate(),
                assignment.getEndDate(),
                assignment.isActive(),
                assignment.getCreatedAt(),
                assignment.getUpdatedAt()
        );
    }
}
