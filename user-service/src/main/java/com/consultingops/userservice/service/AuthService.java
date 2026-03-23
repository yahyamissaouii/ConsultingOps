package com.consultingops.userservice.service;

import com.consultingops.userservice.dto.auth.LoginRequest;
import com.consultingops.userservice.dto.auth.LoginResponse;
import com.consultingops.userservice.entity.Consultant;
import com.consultingops.userservice.entity.User;
import com.consultingops.userservice.exception.UnauthorizedException;
import com.consultingops.userservice.repository.ConsultantRepository;
import com.consultingops.userservice.repository.UserRepository;
import com.consultingops.userservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final ConsultantRepository consultantRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditService auditService;

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!user.isActive() || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        Consultant consultant = consultantRepository.findByUserId(user.getId()).orElse(null);
        String token = jwtService.generateToken(user, consultant);
        auditService.record(user.getId(), "LOGIN_SUCCESS", com.consultingops.userservice.entity.enums.AuditEntityType.AUTH, user.getId(), user.getEmail());
        return new LoginResponse(
                token,
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                consultant == null ? null : consultant.getId()
        );
    }
}
