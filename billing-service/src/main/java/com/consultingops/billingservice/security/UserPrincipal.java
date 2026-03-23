package com.consultingops.billingservice.security;

import com.consultingops.billingservice.entity.enums.UserRole;
import java.util.UUID;

public record UserPrincipal(
        UUID userId,
        String email,
        UserRole role,
        UUID consultantId
) {
}
