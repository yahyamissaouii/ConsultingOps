package com.consultingops.billingservice;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.consultingops.billingservice.client.ApprovedTimeEntryResponse;
import com.consultingops.billingservice.client.TimesheetClient;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
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
class BillingControllerIntegrationTest {

    private static final String JWT_SECRET = "0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF";

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TimesheetClient timesheetClient;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("app.security.jwt-secret", () -> JWT_SECRET);
        registry.add("clients.timesheet-service.url", () -> "http://localhost:8082");
    }

    @Test
    void generateShouldPersistBillingSummaryForBillingAdmin() throws Exception {
        UUID clientId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID consultantId = UUID.randomUUID();
        when(timesheetClient.getApprovedEntries(eq(clientId), eq(projectId), eq(LocalDate.of(2026, 3, 1)), eq(LocalDate.of(2026, 3, 31))))
                .thenReturn(List.of(
                        new ApprovedTimeEntryResponse(
                                UUID.randomUUID(),
                                consultantId,
                                "Maya Patel",
                                projectId,
                                "ERP Modernization",
                                clientId,
                                "Northwind Energy",
                                LocalDate.of(2026, 3, 15),
                                new java.math.BigDecimal("8.00"),
                                new java.math.BigDecimal("120.00"),
                                true
                        )
                ));

        mockMvc.perform(post("/api/v1/billing/generate")
                        .header("Authorization", "Bearer " + billingAdminToken())
                        .contentType("application/json")
                        .content("""
                                {
                                  "clientId": "%s",
                                  "projectId": "%s",
                                  "startDate": "2026-03-01",
                                  "endDate": "2026-03-31",
                                  "currency": "EUR"
                                }
                                """.formatted(clientId, projectId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summaryCount").value(1))
                .andExpect(jsonPath("$.totalAmount").value(960.00));
    }

    @Test
    void anonymousRequestShouldBeRejected() throws Exception {
        mockMvc.perform(get("/api/v1/billing-periods"))
                .andExpect(status().isUnauthorized());
    }

    private String billingAdminToken() {
        SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .claim("email", "billing@consultingops.local")
                .claim("role", "BILLING_ADMIN")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600_000))
                .signWith(key)
                .compact();
    }
}
