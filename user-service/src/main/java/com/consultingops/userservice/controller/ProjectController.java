package com.consultingops.userservice.controller;

import com.consultingops.userservice.dto.common.PageResponse;
import com.consultingops.userservice.dto.project.CreateProjectRequest;
import com.consultingops.userservice.dto.project.ProjectResponse;
import com.consultingops.userservice.dto.project.UpdateProjectRequest;
import com.consultingops.userservice.entity.enums.ProjectStatus;
import com.consultingops.userservice.security.UserPrincipal;
import com.consultingops.userservice.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @Operation(summary = "Create a project")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse create(@Valid @RequestBody CreateProjectRequest request,
                                  @AuthenticationPrincipal UserPrincipal principal) {
        return projectService.create(request, principal.userId());
    }

    @Operation(summary = "List projects with pagination and filters")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','BILLING_ADMIN','CONSULTANT')")
    public PageResponse<ProjectResponse> list(@RequestParam(required = false) UUID clientId,
                                              @RequestParam(required = false) ProjectStatus status,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "20") int size) {
        return projectService.list(clientId, status, page, size);
    }

    @Operation(summary = "Get project details")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','BILLING_ADMIN','CONSULTANT')")
    public ProjectResponse get(@PathVariable UUID id) {
        return projectService.get(id);
    }

    @Operation(summary = "Update a project")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ProjectResponse update(@PathVariable UUID id,
                                  @Valid @RequestBody UpdateProjectRequest request,
                                  @AuthenticationPrincipal UserPrincipal principal) {
        return projectService.update(id, request, principal.userId());
    }
}
