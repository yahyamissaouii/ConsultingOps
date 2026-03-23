package com.consultingops.billingservice.dto.common;

import java.time.OffsetDateTime;
import java.util.List;

public record ApiError(
        OffsetDateTime timestamp,
        int status,
        String code,
        String message,
        String path,
        List<String> details
) {
}
