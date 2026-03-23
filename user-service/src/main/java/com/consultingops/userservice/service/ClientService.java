package com.consultingops.userservice.service;

import com.consultingops.userservice.dto.client.ClientResponse;
import com.consultingops.userservice.dto.client.CreateClientRequest;
import com.consultingops.userservice.dto.client.UpdateClientRequest;
import com.consultingops.userservice.dto.common.PageResponse;
import com.consultingops.userservice.entity.Client;
import com.consultingops.userservice.entity.enums.AuditEntityType;
import com.consultingops.userservice.exception.ConflictException;
import com.consultingops.userservice.exception.ResourceNotFoundException;
import com.consultingops.userservice.repository.ClientRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final AuditService auditService;

    public ClientResponse create(CreateClientRequest request, UUID actorId) {
        ensureTaxIdentifierAvailable(request.taxIdentifier(), null);

        Client client = new Client();
        client.setId(UUID.randomUUID());
        client.setName(request.name());
        client.setContactEmail(request.contactEmail());
        client.setBillingAddress(request.billingAddress());
        client.setTaxIdentifier(request.taxIdentifier());
        client.setActive(request.active() == null || request.active());
        clientRepository.save(client);

        auditService.record(actorId, "CLIENT_CREATED", AuditEntityType.CLIENT, client.getId(), client.getName());
        return toResponse(client);
    }

    public ClientResponse update(UUID id, UpdateClientRequest request, UUID actorId) {
        Client client = findEntity(id);
        ensureTaxIdentifierAvailable(request.taxIdentifier(), id);

        client.setName(request.name());
        client.setContactEmail(request.contactEmail());
        client.setBillingAddress(request.billingAddress());
        client.setTaxIdentifier(request.taxIdentifier());
        client.setActive(request.active());
        clientRepository.save(client);

        auditService.record(actorId, "CLIENT_UPDATED", AuditEntityType.CLIENT, client.getId(), client.getName());
        return toResponse(client);
    }

    public ClientResponse get(UUID id) {
        return toResponse(findEntity(id));
    }

    public PageResponse<ClientResponse> list(String search, int page, int size) {
        var pageable = PageRequest.of(page, size);
        var result = (search == null || search.isBlank())
                ? clientRepository.findAll(pageable)
                : clientRepository.findByNameContainingIgnoreCase(search, pageable);
        return PageResponse.from(result.map(this::toResponse));
    }

    public Client findEntity(UUID id) {
        return clientRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Client not found"));
    }

    private void ensureTaxIdentifierAvailable(String taxIdentifier, UUID currentClientId) {
        if (taxIdentifier == null || taxIdentifier.isBlank()) {
            return;
        }
        clientRepository.findByTaxIdentifier(taxIdentifier)
                .filter(client -> !client.getId().equals(currentClientId))
                .ifPresent(client -> {
                    throw new ConflictException("Tax identifier already exists");
                });
    }

    private ClientResponse toResponse(Client client) {
        return new ClientResponse(
                client.getId(),
                client.getName(),
                client.getContactEmail(),
                client.getBillingAddress(),
                client.getTaxIdentifier(),
                client.isActive(),
                client.getCreatedAt(),
                client.getUpdatedAt()
        );
    }
}
