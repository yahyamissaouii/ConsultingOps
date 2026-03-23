package com.consultingops.timesheetservice.client;

import java.math.BigDecimal;
import java.util.UUID;

public record AssignmentValidationResponse(
        UUID assignmentId,
        UUID consultantId,
        String consultantName,
        UUID projectId,
        String projectName,
        UUID clientId,
        String clientName,
        BigDecimal hourlyRate
) {
}
