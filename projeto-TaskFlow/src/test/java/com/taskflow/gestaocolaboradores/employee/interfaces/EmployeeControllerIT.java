package com.taskflow.gestaocolaboradores.employee.interfaces;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.gestaocolaboradores.employee.interfaces.dto.CreateEmployeeRequest;
import com.taskflow.gestaocolaboradores.shared.domain.Role;
import com.taskflow.gestaocolaboradores.shared.security.dto.LoginRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class EmployeeControllerIT {

    // Jackson 2.x ObjectMapper (não registrado como bean no Boot 4.x / Jackson 3.x)
    private static final ObjectMapper JSON = new ObjectMapper();

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("taskflow")
            .withUsername("taskflow")
            .withPassword("taskflow");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired MockMvc mvc;

    private String adminToken;
    private String managerToken;

    @BeforeEach
    void login() throws Exception {
        adminToken = extractToken("admin@taskflow.com", "password");
        managerToken = extractToken("manager1@taskflow.com", "password");
    }

    private String extractToken(String email, String password) throws Exception {
        String body = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.writeValueAsString(new LoginRequest(email, password))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JSON.readTree(body).get("token").asText();
    }

    // --- Auth tests ---

    @Test
    void login_withValidCredentials_returnsToken() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.writeValueAsString(new LoginRequest("admin@taskflow.com", "password"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void login_withWrongPassword_returns401() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.writeValueAsString(new LoginRequest("admin@taskflow.com", "wrong"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMe_withValidToken_returns200() throws Exception {
        mvc.perform(get("/api/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    // --- Employee access control tests ---

    @Test
    void listEmployees_withoutToken_returns401() throws Exception {
        mvc.perform(get("/api/employees"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listEmployees_asAdmin_returns200() throws Exception {
        mvc.perform(get("/api/employees")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void listEmployees_asManager_returns200() throws Exception {
        mvc.perform(get("/api/employees")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + managerToken))
                .andExpect(status().isOk());
    }

    @Test
    void createEmployee_asManager_returns403() throws Exception {
        var req = new CreateEmployeeRequest("Novo", "novo@x.com", "pass123", Role.COLLABORATOR, null);
        mvc.perform(post("/api/employees")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createEmployee_asAdmin_withInvalidEmail_returns400() throws Exception {
        String body = """
                {"name":"X","email":"not-email","password":"pass123","role":"ADMIN"}
                """;
        mvc.perform(post("/api/employees")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
}
