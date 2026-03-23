package com.consultingops.userservice.repository;

import com.consultingops.userservice.entity.ProjectAssignment;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectAssignmentRepository extends JpaRepository<ProjectAssignment, UUID> {

    List<ProjectAssignment> findByConsultantIdAndActiveTrue(UUID consultantId);

    Page<ProjectAssignment> findByConsultantId(UUID consultantId, Pageable pageable);

    Page<ProjectAssignment> findByProjectId(UUID projectId, Pageable pageable);
}
