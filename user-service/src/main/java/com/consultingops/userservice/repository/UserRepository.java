package com.consultingops.userservice.repository;

import com.consultingops.userservice.entity.User;
import com.consultingops.userservice.entity.enums.UserRole;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByEmailIgnoreCase(String email);

    Optional<User> findByEmailIgnoreCase(String email);

    Page<User> findByRole(UserRole role, Pageable pageable);
}
