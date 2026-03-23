package com.consultingops.userservice.dto.client;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ClientResponse(
        UUID id,
        String name,
        String contactEmail,
        String billingAddress,
        String taxIdentifier,
        boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
