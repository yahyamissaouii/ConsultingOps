package com.consultingops.userservice.controller;

import com.consultingops.userservice.dto.internal.AssignmentValidationResponse;
import com.consultingops.userservice.service.AssignmentService;
import com.consultingops.userservice.service.InternalAuthService;
import io.swagger.v3.oas.annotations.Hidden;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RestController
@RequestMapping("/internal/v1")
@RequiredArgsConstructor
public class InternalDirectoryController {

    private final InternalAuthService internalAuthService;
    private final AssignmentService assignmentService;

    @GetMapping("/assignments/validate")
    public AssignmentValidationResponse validateAssignment(@RequestHeader("X-Internal-Api-Key") String apiKey,
                                                           @RequestParam UUID consultantId,
                                                           @RequestParam UUID projectId,
                                                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate workDate) {
        internalAuthService.assertValid(apiKey);
        return assignmentService.validateAssignment(consultantId, projectId, workDate);
    }
}
