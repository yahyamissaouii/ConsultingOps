package com.consultingops.userservice.controller;

import com.consultingops.userservice.dto.client.ClientResponse;
import com.consultingops.userservice.dto.client.CreateClientRequest;
import com.consultingops.userservice.dto.client.UpdateClientRequest;
import com.consultingops.userservice.dto.common.PageResponse;
import com.consultingops.userservice.security.UserPrincipal;
import com.consultingops.userservice.service.ClientService;
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
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @Operation(summary = "Create a client account")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @ResponseStatus(HttpStatus.CREATED)
    public ClientResponse create(@Valid @RequestBody CreateClientRequest request,
                                 @AuthenticationPrincipal UserPrincipal principal) {
        return clientService.create(request, principal.userId());
    }

    @Operation(summary = "List clients with pagination and search")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','BILLING_ADMIN')")
    public PageResponse<ClientResponse> list(@RequestParam(required = false) String search,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "20") int size) {
        return clientService.list(search, page, size);
    }

    @Operation(summary = "Get client details")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','BILLING_ADMIN')")
    public ClientResponse get(@PathVariable UUID id) {
        return clientService.get(id);
    }

    @Operation(summary = "Update a client account")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ClientResponse update(@PathVariable UUID id,
                                 @Valid @RequestBody UpdateClientRequest request,
                                 @AuthenticationPrincipal UserPrincipal principal) {
        return clientService.update(id, request, principal.userId());
    }
}
