package com.consultingops.userservice.security;

import com.consultingops.userservice.entity.enums.UserRole;
import java.util.UUID;

public record UserPrincipal(
        UUID userId,
        String email,
        UserRole role,
        UUID consultantId
) {
}
