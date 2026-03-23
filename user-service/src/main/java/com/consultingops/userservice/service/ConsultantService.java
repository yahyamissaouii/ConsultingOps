package com.consultingops.userservice.service;

import com.consultingops.userservice.dto.common.PageResponse;
import com.consultingops.userservice.dto.consultant.ConsultantResponse;
import com.consultingops.userservice.dto.consultant.CreateConsultantRequest;
import com.consultingops.userservice.dto.consultant.UpdateConsultantRequest;
import com.consultingops.userservice.entity.Consultant;
import com.consultingops.userservice.entity.User;
import com.consultingops.userservice.entity.enums.AuditEntityType;
import com.consultingops.userservice.entity.enums.ConsultantStatus;
import com.consultingops.userservice.entity.enums.UserRole;
import com.consultingops.userservice.exception.ConflictException;
import com.consultingops.userservice.exception.ResourceNotFoundException;
import com.consultingops.userservice.repository.ConsultantRepository;
import com.consultingops.userservice.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConsultantService {

    private final ConsultantRepository consultantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public ConsultantResponse create(CreateConsultantRequest request, UUID actorId) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ConflictException("Email already exists");
        }
        if (consultantRepository.existsByEmployeeCodeIgnoreCase(request.employeeCode())) {
            throw new ConflictException("Employee code already exists");
        }

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setFullName(request.fullName());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.CONSULTANT);
        user.setActive(true);
        userRepository.save(user);

        Consultant consultant = new Consultant();
        consultant.setId(UUID.randomUUID());
        consultant.setUserId(user.getId());
        consultant.setEmployeeCode(request.employeeCode());
        consultant.setJobTitle(request.jobTitle());
        consultant.setSeniorityLevel(request.seniorityLevel());
        consultant.setHourlyRate(request.hourlyRate());
        consultant.setStatus(request.status());
        consultantRepository.save(consultant);

        auditService.record(actorId, "CONSULTANT_CREATED", AuditEntityType.CONSULTANT, consultant.getId(), consultant.getEmployeeCode());
        return toResponse(consultant, user);
    }

    public ConsultantResponse update(UUID id, UpdateConsultantRequest request, UUID actorId) {
        Consultant consultant = findEntity(id);
        User user = userRepository.findById(consultant.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Linked user not found"));

        user.setFullName(request.fullName());
        user.setActive(request.active());
        userRepository.save(user);

        consultant.setJobTitle(request.jobTitle());
        consultant.setSeniorityLevel(request.seniorityLevel());
        consultant.setHourlyRate(request.hourlyRate());
        consultant.setStatus(request.status());
        consultantRepository.save(consultant);

        auditService.record(actorId, "CONSULTANT_UPDATED", AuditEntityType.CONSULTANT, consultant.getId(), consultant.getEmployeeCode());
        return toResponse(consultant, user);
    }

    public ConsultantResponse get(UUID id) {
        Consultant consultant = findEntity(id);
        User user = userRepository.findById(consultant.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Linked user not found"));
        return toResponse(consultant, user);
    }

    public PageResponse<ConsultantResponse> list(ConsultantStatus status, int page, int size) {
        var pageable = PageRequest.of(page, size);
        var result = status == null
                ? consultantRepository.findAll(pageable)
                : consultantRepository.findByStatus(status, pageable);
        return PageResponse.from(result.map(consultant -> {
            User user = userRepository.findById(consultant.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Linked user not found"));
            return toResponse(consultant, user);
        }));
    }

    public Consultant findEntity(UUID id) {
        return consultantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consultant not found"));
    }

    public Consultant findByUserId(UUID userId) {
        return consultantRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Consultant not found for user"));
    }

    private ConsultantResponse toResponse(Consultant consultant, User user) {
        return new ConsultantResponse(
                consultant.getId(),
                consultant.getUserId(),
                user.getFullName(),
                user.getEmail(),
                consultant.getEmployeeCode(),
                consultant.getJobTitle(),
                consultant.getSeniorityLevel(),
                consultant.getHourlyRate(),
                consultant.getStatus(),
                user.isActive(),
                consultant.getCreatedAt(),
                consultant.getUpdatedAt()
        );
    }
}
