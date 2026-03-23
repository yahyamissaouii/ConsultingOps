package com.consultingops.billingservice.repository;

import com.consultingops.billingservice.entity.BillingSummary;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BillingSummaryRepository extends JpaRepository<BillingSummary, UUID>, JpaSpecificationExecutor<BillingSummary> {

    void deleteByBillingPeriodId(UUID billingPeriodId);
}
