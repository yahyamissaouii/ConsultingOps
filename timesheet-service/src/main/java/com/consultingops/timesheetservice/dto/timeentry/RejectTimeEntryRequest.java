package com.consultingops.timesheetservice.dto.timeentry;

import jakarta.validation.constraints.NotBlank;

public record RejectTimeEntryRequest(@NotBlank String reason) {
}
