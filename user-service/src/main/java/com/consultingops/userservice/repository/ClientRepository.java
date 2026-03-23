package com.consultingops.userservice.repository;

import com.consultingops.userservice.entity.Client;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, UUID> {

    Optional<Client> findByTaxIdentifier(String taxIdentifier);

    Page<Client> findByNameContainingIgnoreCase(String search, Pageable pageable);
}
