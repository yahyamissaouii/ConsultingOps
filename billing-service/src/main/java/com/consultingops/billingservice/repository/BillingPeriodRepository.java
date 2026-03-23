package com.consultingops.billingservice.repository;

import com.consultingops.billingservice.entity.BillingPeriod;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillingPeriodRepository extends JpaRepository<BillingPeriod, UUID> {

    Optional<BillingPeriod> findByClientIdAndStartDateAndEndDate(UUID clientId, LocalDate startDate, LocalDate endDate);
}
