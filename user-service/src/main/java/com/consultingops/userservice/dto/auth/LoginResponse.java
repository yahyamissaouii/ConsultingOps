package com.consultingops.userservice.dto.auth;

import com.consultingops.userservice.entity.enums.UserRole;
import java.util.UUID;

public record LoginResponse(
        String accessToken,
        UUID userId,
        String fullName,
        String email,
        UserRole role,
        UUID consultantId
) {
}
