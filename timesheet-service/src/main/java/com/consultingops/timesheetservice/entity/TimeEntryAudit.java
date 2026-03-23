package com.consultingops.timesheetservice.entity;

import com.consultingops.timesheetservice.entity.enums.TimeEntryStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "time_entry_audits")
public class TimeEntryAudit {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID timeEntryId;

    @Column(nullable = false)
    private UUID actorId;

    @Column(nullable = false)
    private String action;

    @Enumerated(EnumType.STRING)
    private TimeEntryStatus oldStatus;

    @Enumerated(EnumType.STRING)
    private TimeEntryStatus newStatus;

    @Column(length = 1000)
    private String note;

    @Column(nullable = false)
    private OffsetDateTime createdAt;
}
