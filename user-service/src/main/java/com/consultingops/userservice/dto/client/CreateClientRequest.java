package com.consultingops.userservice.dto.client;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateClientRequest(
        @NotBlank String name,
        @Email @NotBlank String contactEmail,
        @NotBlank String billingAddress,
        String taxIdentifier,
        Boolean active
) {
}
