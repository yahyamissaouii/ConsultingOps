package com.consultingops.userservice.service;

import com.consultingops.userservice.dto.common.PageResponse;
import com.consultingops.userservice.dto.project.CreateProjectRequest;
import com.consultingops.userservice.dto.project.ProjectResponse;
import com.consultingops.userservice.dto.project.UpdateProjectRequest;
import com.consultingops.userservice.entity.Client;
import com.consultingops.userservice.entity.Project;
import com.consultingops.userservice.entity.enums.AuditEntityType;
import com.consultingops.userservice.exception.BusinessRuleException;
import com.consultingops.userservice.exception.ConflictException;
import com.consultingops.userservice.exception.ResourceNotFoundException;
import com.consultingops.userservice.repository.ProjectRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ClientService clientService;
    private final AuditService auditService;

    public ProjectResponse create(CreateProjectRequest request, UUID actorId) {
        validateDates(request.startDate(), request.endDate());
        if (projectRepository.existsByCodeIgnoreCase(request.code())) {
            throw new ConflictException("Project code already exists");
        }
        Client client = clientService.findEntity(request.clientId());

        Project project = new Project();
        project.setId(UUID.randomUUID());
        project.setCode(request.code());
        project.setName(request.name());
        project.setDescription(request.description());
        project.setClientId(request.clientId());
        project.setStartDate(request.startDate());
        project.setEndDate(request.endDate());
        project.setBillingModel(request.billingModel());
        project.setStatus(request.status());
        projectRepository.save(project);

        auditService.record(actorId, "PROJECT_CREATED", AuditEntityType.PROJECT, project.getId(), project.getCode());
        return toResponse(project, client);
    }

    public ProjectResponse update(UUID id, UpdateProjectRequest request, UUID actorId) {
        validateDates(request.startDate(), request.endDate());
        Project project = findEntity(id);
        Client client = clientService.findEntity(request.clientId());

        project.setName(request.name());
        project.setDescription(request.description());
        project.setClientId(request.clientId());
        project.setStartDate(request.startDate());
        project.setEndDate(request.endDate());
        project.setBillingModel(request.billingModel());
        project.setStatus(request.status());
        projectRepository.save(project);

        auditService.record(actorId, "PROJECT_UPDATED", AuditEntityType.PROJECT, project.getId(), project.getCode());
        return toResponse(project, client);
    }

    public ProjectResponse get(UUID id) {
        Project project = findEntity(id);
        Client client = clientService.findEntity(project.getClientId());
        return toResponse(project, client);
    }

    public PageResponse<ProjectResponse> list(UUID clientId, com.consultingops.userservice.entity.enums.ProjectStatus status, int page, int size) {
        var pageable = PageRequest.of(page, size);
        var result = clientId != null
                ? projectRepository.findByClientId(clientId, pageable)
                : status != null ? projectRepository.findByStatus(status, pageable) : projectRepository.findAll(pageable);
        return PageResponse.from(result.map(project -> toResponse(project, clientService.findEntity(project.getClientId()))));
    }

    public Project findEntity(UUID id) {
        return projectRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    }

    private void validateDates(java.time.LocalDate startDate, java.time.LocalDate endDate) {
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new BusinessRuleException("Project end date must be after start date");
        }
    }

    private ProjectResponse toResponse(Project project, Client client) {
        return new ProjectResponse(
                project.getId(),
                project.getCode(),
                project.getName(),
                project.getDescription(),
                project.getClientId(),
                client.getName(),
                project.getStartDate(),
                project.getEndDate(),
                project.getBillingModel(),
                project.getStatus(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}
