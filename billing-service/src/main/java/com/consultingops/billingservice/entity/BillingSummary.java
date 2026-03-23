package com.consultingops.billingservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "billing_summaries")
public class BillingSummary {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID billingPeriodId;

    @Column(nullable = false)
    private UUID clientId;

    @Column(nullable = false)
    private String clientNameSnapshot;

    @Column(nullable = false)
    private UUID projectId;

    @Column(nullable = false)
    private String projectNameSnapshot;

    @Column(nullable = false)
    private UUID consultantId;

    @Column(nullable = false)
    private String consultantNameSnapshot;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal approvedHours;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal hourlyRate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private OffsetDateTime generatedAt;
}
