package com.consultingops.billingservice.service;

import com.consultingops.billingservice.client.ApprovedTimeEntryResponse;
import com.consultingops.billingservice.client.TimesheetClient;
import com.consultingops.billingservice.exception.ExternalServiceException;
import feign.FeignException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TimesheetAggregationService {

    private final TimesheetClient timesheetClient;

    public List<ApprovedTimeEntryResponse> getApprovedEntries(UUID clientId, UUID projectId, LocalDate startDate, LocalDate endDate) {
        try {
            return timesheetClient.getApprovedEntries(clientId, projectId, startDate, endDate);
        } catch (FeignException exception) {
            throw new ExternalServiceException("Unable to retrieve approved time entries from timesheet-service");
        }
    }
}
