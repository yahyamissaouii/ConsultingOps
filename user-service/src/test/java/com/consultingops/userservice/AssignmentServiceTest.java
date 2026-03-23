package com.consultingops.userservice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.consultingops.userservice.dto.assignment.CreateProjectAssignmentRequest;
import com.consultingops.userservice.entity.Consultant;
import com.consultingops.userservice.entity.Project;
import com.consultingops.userservice.entity.ProjectAssignment;
import com.consultingops.userservice.entity.User;
import com.consultingops.userservice.entity.enums.ConsultantStatus;
import com.consultingops.userservice.entity.enums.ProjectStatus;
import com.consultingops.userservice.entity.enums.SeniorityLevel;
import com.consultingops.userservice.entity.enums.UserRole;
import com.consultingops.userservice.exception.BusinessRuleException;
import com.consultingops.userservice.repository.ProjectAssignmentRepository;
import com.consultingops.userservice.repository.UserRepository;
import com.consultingops.userservice.service.AssignmentService;
import com.consultingops.userservice.service.AuditService;
import com.consultingops.userservice.service.ClientService;
import com.consultingops.userservice.service.ConsultantService;
import com.consultingops.userservice.service.ProjectService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AssignmentServiceTest {

    @Mock
    private ProjectAssignmentRepository assignmentRepository;

    @Mock
    private ConsultantService consultantService;

    @Mock
    private ProjectService projectService;

    @Mock
    private ClientService clientService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AssignmentService assignmentService;

    @Test
    void createShouldRejectOverAllocation() {
        UUID consultantId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        when(consultantService.findEntity(consultantId)).thenReturn(activeConsultant(consultantId));
        when(projectService.findEntity(projectId)).thenReturn(activeProject(projectId));
        when(assignmentRepository.findByConsultantIdAndActiveTrue(consultantId))
                .thenReturn(List.of(existingAssignment(consultantId, 60, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31))));

        CreateProjectAssignmentRequest request = new CreateProjectAssignmentRequest(
                consultantId,
                projectId,
                "Backend Lead",
                50,
                LocalDate.of(2026, 3, 10),
                LocalDate.of(2026, 3, 20),
                true
        );

        assertThatThrownBy(() -> assignmentService.create(request, UUID.randomUUID()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("100% allocation");
    }

    @Test
    void createShouldPersistValidAssignment() {
        UUID consultantId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Consultant consultant = activeConsultant(consultantId);
        consultant.setUserId(userId);
        Project project = activeProject(projectId);
        User user = new User();
        user.setId(userId);
        user.setFullName("Maya Patel");
        user.setEmail("maya@consultingops.local");
        user.setRole(UserRole.CONSULTANT);
        user.setActive(true);

        when(consultantService.findEntity(consultantId)).thenReturn(consultant);
        when(projectService.findEntity(projectId)).thenReturn(project);
        when(assignmentRepository.findByConsultantIdAndActiveTrue(consultantId)).thenReturn(List.of());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(assignmentRepository.save(any(ProjectAssignment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateProjectAssignmentRequest request = new CreateProjectAssignmentRequest(
                consultantId,
                projectId,
                "Backend Lead",
                80,
                LocalDate.of(2026, 3, 10),
                LocalDate.of(2026, 3, 20),
                true
        );

        var response = assignmentService.create(request, UUID.randomUUID());

        assertThat(response.consultantId()).isEqualTo(consultantId);
        assertThat(response.projectId()).isEqualTo(projectId);
        assertThat(response.consultantName()).isEqualTo("Maya Patel");
        verify(assignmentRepository).save(any(ProjectAssignment.class));
    }

    private Consultant activeConsultant(UUID consultantId) {
        Consultant consultant = new Consultant();
        consultant.setId(consultantId);
        consultant.setUserId(UUID.randomUUID());
        consultant.setEmployeeCode("EMP-2001");
        consultant.setJobTitle("Senior Consultant");
        consultant.setSeniorityLevel(SeniorityLevel.SENIOR);
        consultant.setHourlyRate(new BigDecimal("120.00"));
        consultant.setStatus(ConsultantStatus.ACTIVE);
        return consultant;
    }

    private Project activeProject(UUID projectId) {
        Project project = new Project();
        project.setId(projectId);
        project.setClientId(UUID.randomUUID());
        project.setCode("PRJ-01");
        project.setName("ERP Modernization");
        project.setStartDate(LocalDate.of(2026, 3, 1));
        project.setEndDate(LocalDate.of(2026, 6, 30));
        project.setStatus(ProjectStatus.ACTIVE);
        return project;
    }

    private ProjectAssignment existingAssignment(UUID consultantId, int allocation, LocalDate startDate, LocalDate endDate) {
        ProjectAssignment assignment = new ProjectAssignment();
        assignment.setId(UUID.randomUUID());
        assignment.setConsultantId(consultantId);
        assignment.setProjectId(UUID.randomUUID());
        assignment.setAssignedRole("Existing");
        assignment.setAllocationPercentage(allocation);
        assignment.setStartDate(startDate);
        assignment.setEndDate(endDate);
        assignment.setActive(true);
        return assignment;
    }
}
