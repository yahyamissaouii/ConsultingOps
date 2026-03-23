package com.consultingops.billingservice.client;

import com.consultingops.billingservice.config.InternalFeignConfig;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "timesheetClient",
        url = "${clients.timesheet-service.url}",
        configuration = InternalFeignConfig.class
)
public interface TimesheetClient {

    @GetMapping("/internal/v1/time-entries/approved")
    List<ApprovedTimeEntryResponse> getApprovedEntries(
            @RequestParam UUID clientId,
            @RequestParam(required = false) UUID projectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    );
}
