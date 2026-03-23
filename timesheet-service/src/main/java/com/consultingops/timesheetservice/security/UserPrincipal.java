package com.consultingops.timesheetservice.security;

import com.consultingops.timesheetservice.entity.enums.UserRole;
import java.util.UUID;

public record UserPrincipal(
        UUID userId,
        String email,
        UserRole role,
        UUID consultantId
) {
}
