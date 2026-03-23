package com.consultingops.timesheetservice.entity;

import com.consultingops.timesheetservice.entity.enums.TimeEntryStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "time_entries")
public class TimeEntry extends BaseEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID consultantId;

    @Column(nullable = false)
    private UUID projectId;

    @Column(nullable = false)
    private UUID clientId;

    @Column(nullable = false)
    private String consultantNameSnapshot;

    @Column(nullable = false)
    private String projectNameSnapshot;

    @Column(nullable = false)
    private String clientNameSnapshot;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal hourlyRateSnapshot;

    @Column(nullable = false)
    private LocalDate workDate;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal hours;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false)
    private boolean billable;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TimeEntryStatus status;

    private OffsetDateTime submittedAt;

    private OffsetDateTime approvedAt;

    private UUID approvedBy;

    @Column(length = 1000)
    private String rejectionReason;
}
