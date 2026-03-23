package com.consultingops.billingservice.repository;

import com.consultingops.billingservice.entity.BillingAuditEvent;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillingAuditEventRepository extends JpaRepository<BillingAuditEvent, UUID> {
}
