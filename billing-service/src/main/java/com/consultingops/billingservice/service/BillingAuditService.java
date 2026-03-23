package com.consultingops.billingservice.service;

import com.consultingops.billingservice.dto.audit.BillingAuditResponse;
import com.consultingops.billingservice.dto.common.PageResponse;
import com.consultingops.billingservice.entity.BillingAuditEvent;
import com.consultingops.billingservice.repository.BillingAuditEventRepository;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BillingAuditService {

    private final BillingAuditEventRepository billingAuditEventRepository;

    public void record(UUID actorId, String action, String entityType, UUID entityId, String metadata) {
        BillingAuditEvent audit = new BillingAuditEvent();
        audit.setId(UUID.randomUUID());
        audit.setActorId(actorId);
        audit.setAction(action);
        audit.setEntityType(entityType);
        audit.setEntityId(entityId);
        audit.setMetadata(metadata);
        audit.setCreatedAt(OffsetDateTime.now());
        billingAuditEventRepository.save(audit);
    }

    public PageResponse<BillingAuditResponse> list(int page, int size) {
        return PageResponse.from(billingAuditEventRepository.findAll(PageRequest.of(page, size)).map(this::toResponse));
    }

    private BillingAuditResponse toResponse(BillingAuditEvent event) {
        return new BillingAuditResponse(
                event.getId(),
                event.getActorId(),
                event.getAction(),
                event.getEntityType(),
                event.getEntityId(),
                event.getMetadata(),
                event.getCreatedAt()
        );
    }
}
