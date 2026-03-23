package com.consultingops.timesheetservice;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.consultingops.timesheetservice.client.AssignmentValidationResponse;
import com.consultingops.timesheetservice.client.UserDirectoryClient;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@AutoConfigureMockMvc
class TimeEntryControllerIntegrationTest {

    private static final String JWT_SECRET = "0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF";

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserDirectoryClient userDirectoryClient;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("app.security.jwt-secret", () -> JWT_SECRET);
        registry.add("clients.user-service.url", () -> "http://localhost:8081");
    }

    @Test
    void createDraftEntryShouldPersistWhenAssignmentIsValid() throws Exception {
        UUID consultantId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        when(userDirectoryClient.validateAssignment(eq(consultantId), eq(projectId), eq(LocalDate.of(2026, 3, 20))))
                .thenReturn(new AssignmentValidationResponse(
                        UUID.randomUUID(),
                        consultantId,
                        "Maya Patel",
                        projectId,
                        "ERP Modernization",
                        UUID.randomUUID(),
                        "Northwind Energy",
                        new java.math.BigDecimal("120.00")
                ));

        mockMvc.perform(post("/api/v1/time-entries")
                        .header("Authorization", "Bearer " + consultantToken(consultantId))
                        .contentType("application/json")
                        .content("""
                                {
                                  "consultantId": "%s",
                                  "projectId": "%s",
                                  "workDate": "2026-03-20",
                                  "hours": 8.0,
                                  "description": "Implemented billing export",
                                  "billable": true
                                }
                                """.formatted(consultantId, projectId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.consultantName").value("Maya Patel"));
    }

    @Test
    void anonymousRequestShouldBeRejected() throws Exception {
        mockMvc.perform(get("/api/v1/time-entries"))
                .andExpect(status().isUnauthorized());
    }

    private String consultantToken(UUID consultantId) {
        SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .claim("email", "consultant@consultingops.local")
                .claim("role", "CONSULTANT")
                .claim("consultantId", consultantId.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600_000))
                .signWith(key)
                .compact();
    }
}
