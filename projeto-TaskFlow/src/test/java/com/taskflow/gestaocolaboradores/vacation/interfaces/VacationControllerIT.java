package com.taskflow.gestaocolaboradores.vacation.interfaces;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.gestaocolaboradores.shared.security.dto.LoginRequest;
import com.taskflow.gestaocolaboradores.vacation.infrastructure.VacationJpaRepository;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class VacationControllerIT {

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
    @Autowired VacationJpaRepository vacationRepo;

    private String adminToken;
    private String manager1Token;
    private String carlaToken;
    private String caioToken;
    private String crisToken;

    @BeforeEach
    void setUp() throws Exception {
        vacationRepo.deleteAll();
        adminToken    = login("admin@taskflow.com",    "password");
        manager1Token = login("manager1@taskflow.com", "password");
        carlaToken    = login("carla@taskflow.com",    "password");
        caioToken     = login("caio@taskflow.com",     "password");
        crisToken     = login("cris@taskflow.com",     "password");
    }

    private String login(String email, String password) throws Exception {
        String body = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.writeValueAsString(new LoginRequest(email, password))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JSON.readTree(body).get("token").asText();
    }

    private String createVacation(String token, String startDate, String endDate) throws Exception {
        String payload = String.format(
                "{\"startDate\":\"%s\",\"endDate\":\"%s\"}", startDate, endDate);
        return mvc.perform(post("/api/vacations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
    }

    private String extractId(String json) throws Exception {
        return JSON.readTree(json).get("id").asText();
    }

    @Test
    void listVacations_withoutToken_returns401_withEnvelope() throws Exception {
        mvc.perform(get("/api/vacations"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/api/vacations"));
    }

    @Test
    void createVacation_asCollaborator_returns201_PENDING() throws Exception {
        mvc.perform(post("/api/vacations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + carlaToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"startDate\":\"2026-08-01\",\"endDate\":\"2026-08-05\",\"reason\":\"férias\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.employeeId").value("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void createVacation_endBeforeStart_returns400_VALIDATION_ERROR() throws Exception {
        mvc.perform(post("/api/vacations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + carlaToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"startDate\":\"2026-08-10\",\"endDate\":\"2026-08-05\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void createVacation_missingStartDate_returns400() throws Exception {
        mvc.perform(post("/api/vacations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + carlaToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"endDate\":\"2026-08-05\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void getVacation_nonexistent_returns404_NOT_FOUND() throws Exception {
        mvc.perform(get("/api/vacations/00000000-0000-0000-0000-000000000001")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void approveVacation_asCollaborator_returns403_FORBIDDEN() throws Exception {
        String resp = createVacation(carlaToken, "2026-09-01", "2026-09-05");
        String id = extractId(resp);

        mvc.perform(post("/api/vacations/" + id + "/approve")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + carlaToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void approveVacation_byWrongManager_returns403() throws Exception {
        String resp = createVacation(crisToken, "2026-09-10", "2026-09-15");
        String id = extractId(resp);

        mvc.perform(post("/api/vacations/" + id + "/approve")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + manager1Token))
                .andExpect(status().isForbidden());
    }

    @Test
    void fullFlow_createAndApprove_returnsAPPROVED() throws Exception {
        String resp = createVacation(carlaToken, "2026-10-01", "2026-10-05");
        String id = extractId(resp);

        mvc.perform(post("/api/vacations/" + id + "/approve")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + manager1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.decidedBy").exists());
    }

    @Test
    void createVacation_afterAnotherApproved_sameperiod_returns409_VACATION_OVERLAP() throws Exception {
        String carlaResp = createVacation(carlaToken, "2026-10-01", "2026-10-05");
        mvc.perform(post("/api/vacations/" + extractId(carlaResp) + "/approve")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + manager1Token));

        mvc.perform(post("/api/vacations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + caioToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"startDate\":\"2026-10-01\",\"endDate\":\"2026-10-05\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("VACATION_OVERLAP"))
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void approveAlreadyApproved_returns409_INVALID_STATE_TRANSITION() throws Exception {
        String resp = createVacation(carlaToken, "2026-11-01", "2026-11-05");
        String id = extractId(resp);

        mvc.perform(post("/api/vacations/" + id + "/approve")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + manager1Token))
                .andExpect(status().isOk());

        mvc.perform(post("/api/vacations/" + id + "/approve")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + manager1Token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_STATE_TRANSITION"));
    }

    @Test
    void updateApprovedVacation_returns409_INVALID_STATE_TRANSITION() throws Exception {
        String resp = createVacation(carlaToken, "2026-11-10", "2026-11-15");
        String id = extractId(resp);

        mvc.perform(post("/api/vacations/" + id + "/approve")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + manager1Token));

        mvc.perform(put("/api/vacations/" + id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + carlaToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"startDate\":\"2026-11-10\",\"endDate\":\"2026-11-20\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_STATE_TRANSITION"));
    }

    @Test
    void calendarEndpoint_returnsApprovedVacationsInPeriod() throws Exception {
        String resp = createVacation(carlaToken, "2026-07-01", "2026-07-05");
        mvc.perform(post("/api/vacations/" + extractId(resp) + "/approve")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + manager1Token));

        mvc.perform(get("/api/vacations/calendar?from=2026-07-01&to=2026-07-31")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].startDate").value("2026-07-01"))
                .andExpect(jsonPath("$[0].status").value("APPROVED"));
    }
}