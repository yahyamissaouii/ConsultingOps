package com.consultingops.userservice.bootstrap;

import com.consultingops.userservice.entity.Client;
import com.consultingops.userservice.entity.Consultant;
import com.consultingops.userservice.entity.Project;
import com.consultingops.userservice.entity.ProjectAssignment;
import com.consultingops.userservice.entity.User;
import com.consultingops.userservice.entity.enums.BillingModel;
import com.consultingops.userservice.entity.enums.ConsultantStatus;
import com.consultingops.userservice.entity.enums.ProjectStatus;
import com.consultingops.userservice.entity.enums.SeniorityLevel;
import com.consultingops.userservice.entity.enums.UserRole;
import com.consultingops.userservice.repository.ClientRepository;
import com.consultingops.userservice.repository.ConsultantRepository;
import com.consultingops.userservice.repository.ProjectAssignmentRepository;
import com.consultingops.userservice.repository.ProjectRepository;
import com.consultingops.userservice.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DemoDataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ConsultantRepository consultantRepository;
    private final ClientRepository clientRepository;
    private final ProjectRepository projectRepository;
    private final ProjectAssignmentRepository projectAssignmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.demo-data.enabled:true}")
    private boolean demoDataEnabled;

    @Override
    public void run(String... args) {
        if (!demoDataEnabled || userRepository.existsByEmailIgnoreCase("admin@consultingops.local")) {
            return;
        }

        User admin = saveUser("Platform Admin", "admin@consultingops.local", "Admin123!", UserRole.ADMIN);
        saveUser("Practice Manager", "manager@consultingops.local", "Manager123!", UserRole.MANAGER);
        saveUser("Billing Lead", "billing@consultingops.local", "Billing123!", UserRole.BILLING_ADMIN);
        User consultantUser = saveUser("Maya Patel", "consultant@consultingops.local", "Consultant123!", UserRole.CONSULTANT);

        Consultant consultant = new Consultant();
        consultant.setId(UUID.randomUUID());
        consultant.setUserId(consultantUser.getId());
        consultant.setEmployeeCode("EMP-1001");
        consultant.setJobTitle("Senior Java Consultant");
        consultant.setSeniorityLevel(SeniorityLevel.SENIOR);
        consultant.setHourlyRate(new BigDecimal("120.00"));
        consultant.setStatus(ConsultantStatus.ACTIVE);
        consultantRepository.save(consultant);

        Client client = new Client();
        client.setId(UUID.randomUUID());
        client.setName("Northwind Energy");
        client.setContactEmail("ap@northwind.example");
        client.setBillingAddress("100 Harbor Street, Rotterdam");
        client.setTaxIdentifier("NW-ENERGY-001");
        client.setActive(true);
        clientRepository.save(client);

        Project project = new Project();
        project.setId(UUID.randomUUID());
        project.setCode("NWE-ERP-01");
        project.setName("ERP Modernization");
        project.setDescription("Spring-based modernization program for finance and resource planning.");
        project.setClientId(client.getId());
        project.setStartDate(LocalDate.now().minusMonths(1));
        project.setEndDate(LocalDate.now().plusMonths(6));
        project.setBillingModel(BillingModel.TIME_AND_MATERIALS);
        project.setStatus(ProjectStatus.ACTIVE);
        projectRepository.save(project);

        ProjectAssignment assignment = new ProjectAssignment();
        assignment.setId(UUID.randomUUID());
        assignment.setConsultantId(consultant.getId());
        assignment.setProjectId(project.getId());
        assignment.setAssignedRole("Backend Lead");
        assignment.setAllocationPercentage(100);
        assignment.setStartDate(LocalDate.now().minusWeeks(2));
        assignment.setEndDate(LocalDate.now().plusMonths(3));
        assignment.setActive(true);
        projectAssignmentRepository.save(assignment);
    }

    private User saveUser(String fullName, String email, String rawPassword, UserRole role) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        user.setActive(true);
        return userRepository.save(user);
    }
}
