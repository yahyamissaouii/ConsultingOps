package com.consultingops.userservice.dto.user;

import com.consultingops.userservice.entity.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank String fullName,
        @Email @NotBlank String email,
        @NotBlank @Size(min = 8, max = 128) String password,
        @NotNull UserRole role,
        Boolean active
) {
}
