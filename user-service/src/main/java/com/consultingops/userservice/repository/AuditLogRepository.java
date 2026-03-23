package com.consultingops.userservice.repository;

import com.consultingops.userservice.entity.AuditLog;
import com.consultingops.userservice.entity.enums.AuditEntityType;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    Page<AuditLog> findByEntityType(AuditEntityType entityType, Pageable pageable);
}
