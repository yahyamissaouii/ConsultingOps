package com.consultingops.userservice.controller;

import com.consultingops.userservice.dto.common.PageResponse;
import com.consultingops.userservice.dto.user.CreateUserRequest;
import com.consultingops.userservice.dto.user.UpdateUserRequest;
import com.consultingops.userservice.dto.user.UserResponse;
import com.consultingops.userservice.entity.enums.UserRole;
import com.consultingops.userservice.security.UserPrincipal;
import com.consultingops.userservice.service.UserManagementService;
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
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserManagementService userManagementService;

    @Operation(summary = "Create an internal platform user")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody CreateUserRequest request,
                               @AuthenticationPrincipal UserPrincipal principal) {
        return userManagementService.create(request, principal.userId());
    }

    @Operation(summary = "List internal users with pagination")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public PageResponse<UserResponse> list(@RequestParam(required = false) UserRole role,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "20") int size) {
        return userManagementService.list(role, page, size);
    }

    @Operation(summary = "Get a single platform user")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public UserResponse get(@PathVariable UUID id) {
        return userManagementService.get(id);
    }

    @Operation(summary = "Update an internal platform user")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse update(@PathVariable UUID id,
                               @Valid @RequestBody UpdateUserRequest request,
                               @AuthenticationPrincipal UserPrincipal principal) {
        return userManagementService.update(id, request, principal.userId());
    }
}
