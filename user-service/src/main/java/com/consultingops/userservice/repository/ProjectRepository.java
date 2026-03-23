package com.consultingops.userservice.repository;

import com.consultingops.userservice.entity.Project;
import com.consultingops.userservice.entity.enums.ProjectStatus;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    boolean existsByCodeIgnoreCase(String code);

    Page<Project> findByStatus(ProjectStatus status, Pageable pageable);

    Page<Project> findByClientId(UUID clientId, Pageable pageable);
}
