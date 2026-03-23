package com.consultingops.userservice.controller;

import com.consultingops.userservice.dto.common.PageResponse;
import com.consultingops.userservice.dto.consultant.ConsultantResponse;
import com.consultingops.userservice.dto.consultant.CreateConsultantRequest;
import com.consultingops.userservice.dto.consultant.UpdateConsultantRequest;
import com.consultingops.userservice.entity.enums.ConsultantStatus;
import com.consultingops.userservice.security.UserPrincipal;
import com.consultingops.userservice.service.ConsultantService;
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
@RequestMapping("/api/v1/consultants")
@RequiredArgsConstructor
public class ConsultantController {

    private final ConsultantService consultantService;

    @Operation(summary = "Create a consultant profile and linked consultant user account")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @ResponseStatus(HttpStatus.CREATED)
    public ConsultantResponse create(@Valid @RequestBody CreateConsultantRequest request,
                                     @AuthenticationPrincipal UserPrincipal principal) {
        return consultantService.create(request, principal.userId());
    }

    @Operation(summary = "List consultants with pagination")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public PageResponse<ConsultantResponse> list(@RequestParam(required = false) ConsultantStatus status,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        return consultantService.list(status, page, size);
    }

    @Operation(summary = "Get consultant details")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','CONSULTANT')")
    public ConsultantResponse get(@PathVariable UUID id,
                                  @AuthenticationPrincipal UserPrincipal principal) {
        if (principal.role() == com.consultingops.userservice.entity.enums.UserRole.CONSULTANT
                && !id.equals(principal.consultantId())) {
            throw new com.consultingops.userservice.exception.UnauthorizedException("Consultants can only access their own profile");
        }
        return consultantService.get(id);
    }

    @Operation(summary = "Update consultant details")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ConsultantResponse update(@PathVariable UUID id,
                                     @Valid @RequestBody UpdateConsultantRequest request,
                                     @AuthenticationPrincipal UserPrincipal principal) {
        return consultantService.update(id, request, principal.userId());
    }
}
