package com.consultingops.userservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "project_assignments")
public class ProjectAssignment extends BaseEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID consultantId;

    @Column(nullable = false)
    private UUID projectId;

    @Column(nullable = false)
    private String assignedRole;

    @Column(nullable = false)
    private Integer allocationPercentage;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    @Column(nullable = false)
    private boolean active = true;
}
