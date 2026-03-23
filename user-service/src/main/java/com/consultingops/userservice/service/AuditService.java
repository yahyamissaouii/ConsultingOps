package com.consultingops.userservice.service;

import com.consultingops.userservice.dto.audit.AuditLogResponse;
import com.consultingops.userservice.dto.common.PageResponse;
import com.consultingops.userservice.entity.AuditLog;
import com.consultingops.userservice.entity.enums.AuditEntityType;
import com.consultingops.userservice.repository.AuditLogRepository;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void record(UUID actorId, String action, AuditEntityType entityType, UUID entityId, String metadata) {
        AuditLog log = new AuditLog();
        log.setId(UUID.randomUUID());
        log.setActorId(actorId);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setMetadata(metadata);
        log.setCreatedAt(OffsetDateTime.now());
        auditLogRepository.save(log);
    }

    public PageResponse<AuditLogResponse> list(AuditEntityType entityType, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        var result = entityType == null
                ? auditLogRepository.findAll(pageable)
                : auditLogRepository.findByEntityType(entityType, pageable);
        return PageResponse.from(result.map(this::toResponse));
    }

    private AuditLogResponse toResponse(AuditLog auditLog) {
        return new AuditLogResponse(
                auditLog.getId(),
                auditLog.getActorId(),
                auditLog.getAction(),
                auditLog.getEntityType(),
                auditLog.getEntityId(),
                auditLog.getMetadata(),
                auditLog.getCreatedAt()
        );
    }
}
