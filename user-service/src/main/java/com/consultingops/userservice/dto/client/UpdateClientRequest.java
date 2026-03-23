package com.consultingops.userservice.dto.client;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateClientRequest(
        @NotBlank String name,
        @Email @NotBlank String contactEmail,
        @NotBlank String billingAddress,
        String taxIdentifier,
        @NotNull Boolean active
) {
}
