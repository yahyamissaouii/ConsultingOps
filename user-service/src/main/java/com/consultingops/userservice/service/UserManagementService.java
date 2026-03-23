package com.consultingops.userservice.service;

import com.consultingops.userservice.dto.common.PageResponse;
import com.consultingops.userservice.dto.user.CreateUserRequest;
import com.consultingops.userservice.dto.user.UpdateUserRequest;
import com.consultingops.userservice.dto.user.UserResponse;
import com.consultingops.userservice.entity.User;
import com.consultingops.userservice.entity.enums.AuditEntityType;
import com.consultingops.userservice.entity.enums.UserRole;
import com.consultingops.userservice.exception.BusinessRuleException;
import com.consultingops.userservice.exception.ConflictException;
import com.consultingops.userservice.exception.ResourceNotFoundException;
import com.consultingops.userservice.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
public class UserManagementService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public UserResponse create(CreateUserRequest request, UUID actorId) {
        if (request.role() == UserRole.CONSULTANT) {
            throw new BusinessRuleException("Consultant accounts must be created via the consultant endpoint");
        }
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ConflictException("Email already exists");
        }

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setFullName(request.fullName());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        user.setActive(request.active() == null || request.active());
        userRepository.save(user);
        auditService.record(actorId, "USER_CREATED", AuditEntityType.USER, user.getId(), user.getEmail());
        return toResponse(user);
    }

    public UserResponse update(UUID id, UpdateUserRequest request, UUID actorId) {
        User user = findEntity(id);
        if (user.getRole() == UserRole.CONSULTANT && request.role() != UserRole.CONSULTANT) {
            throw new BusinessRuleException("Consultant-linked users cannot change role through the user endpoint");
        }
        user.setFullName(request.fullName());
        user.setRole(request.role());
        user.setActive(request.active());
        userRepository.save(user);
        auditService.record(actorId, "USER_UPDATED", AuditEntityType.USER, user.getId(), user.getEmail());
        return toResponse(user);
    }

    public UserResponse get(UUID id) {
        return toResponse(findEntity(id));
    }

    public PageResponse<UserResponse> list(UserRole role, int page, int size) {
        var pageable = PageRequest.of(page, size);
        var result = role == null ? userRepository.findAll(pageable) : userRepository.findByRole(role, pageable);
        return PageResponse.from(result.map(this::toResponse));
    }

    public User findEntity(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
