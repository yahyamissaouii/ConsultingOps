package com.consultingops.timesheetservice.client;

import com.consultingops.timesheetservice.config.InternalFeignConfig;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "userDirectoryClient",
        url = "${clients.user-service.url}",
        configuration = InternalFeignConfig.class
)
public interface UserDirectoryClient {

    @GetMapping("/internal/v1/assignments/validate")
    AssignmentValidationResponse validateAssignment(
            @RequestParam UUID consultantId,
            @RequestParam UUID projectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate workDate
    );
}
