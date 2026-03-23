package com.consultingops.userservice.controller;

import com.consultingops.userservice.dto.assignment.CreateProjectAssignmentRequest;
import com.consultingops.userservice.dto.assignment.ProjectAssignmentResponse;
import com.consultingops.userservice.dto.common.PageResponse;
import com.consultingops.userservice.security.UserPrincipal;
import com.consultingops.userservice.service.AssignmentService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    @Operation(summary = "Assign a consultant to a project")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectAssignmentResponse create(@Valid @RequestBody CreateProjectAssignmentRequest request,
                                            @AuthenticationPrincipal UserPrincipal principal) {
        return assignmentService.create(request, principal.userId());
    }

    @Operation(summary = "List project assignments")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','CONSULTANT')")
    public PageResponse<ProjectAssignmentResponse> list(@RequestParam(required = false) UUID consultantId,
                                                        @RequestParam(required = false) UUID projectId,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "20") int size,
                                                        @AuthenticationPrincipal UserPrincipal principal) {
        UUID effectiveConsultantId = principal.role() == com.consultingops.userservice.entity.enums.UserRole.CONSULTANT
                ? principal.consultantId()
                : consultantId;
        return assignmentService.list(effectiveConsultantId, projectId, page, size);
    }

    @Operation(summary = "Get a single project assignment")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','CONSULTANT')")
    public ProjectAssignmentResponse get(@PathVariable UUID id) {
        return assignmentService.get(id);
    }
}
