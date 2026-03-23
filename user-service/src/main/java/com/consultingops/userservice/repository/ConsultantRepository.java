package com.consultingops.userservice.repository;

import com.consultingops.userservice.entity.Consultant;
import com.consultingops.userservice.entity.enums.ConsultantStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultantRepository extends JpaRepository<Consultant, UUID> {

    boolean existsByEmployeeCodeIgnoreCase(String employeeCode);

    Optional<Consultant> findByUserId(UUID userId);

    Page<Consultant> findByStatus(ConsultantStatus status, Pageable pageable);

    List<Consultant> findByIdIn(List<UUID> ids);
}
