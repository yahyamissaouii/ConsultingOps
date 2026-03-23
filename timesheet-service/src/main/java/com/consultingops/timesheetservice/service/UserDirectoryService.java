package com.consultingops.timesheetservice.service;

import com.consultingops.timesheetservice.client.AssignmentValidationResponse;
import com.consultingops.timesheetservice.client.UserDirectoryClient;
import com.consultingops.timesheetservice.exception.BusinessRuleException;
import com.consultingops.timesheetservice.exception.ExternalServiceException;
import feign.FeignException;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDirectoryService {

    private final UserDirectoryClient userDirectoryClient;

    public AssignmentValidationResponse validateAssignment(UUID consultantId, UUID projectId, LocalDate workDate) {
        try {
            return userDirectoryClient.validateAssignment(consultantId, projectId, workDate);
        } catch (FeignException.NotFound | FeignException.BadRequest exception) {
            throw new BusinessRuleException("Consultant is not actively assigned to the target project for the requested work date");
        } catch (FeignException exception) {
            throw new ExternalServiceException("Unable to validate consultant/project references against user-service");
        }
    }
}
