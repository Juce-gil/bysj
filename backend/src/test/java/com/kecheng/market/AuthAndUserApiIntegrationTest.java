package com.kecheng.market;

import com.fasterxml.jackson.databind.JsonNode;
import com.kecheng.market.support.AbstractApiIntegrationTest;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthAndUserApiIntegrationTest extends AbstractApiIntegrationTest {

    @Test
    void registerLoginAndUpdateProfileFlowWorks() throws Exception {
        String username = "user" + System.nanoTime();
        String password = "secret123";

        ResultActions registerResult = postJson("/api/auth/register", Map.of(
                "username", username,
                "nickname", "New User",
                "password", password,
                "confirmPassword", password
        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.role").value("user"))
                .andExpect(jsonPath("$.data.name").value("New User"));

        long userId = responseData(registerResult).path("id").asLong();
        String token = loginAndGetToken(username, password);

        getJson("/api/users/me", token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(userId))
                .andExpect(jsonPath("$.data.name").value("New User"));

        putJson("/api/users/me", token, Map.of(
                "name", "Renamed User",
                "phone", "13900000000",
                "qq", "445566",
                "slogan", "Updated slogan"
        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("Renamed User"))
                .andExpect(jsonPath("$.data.phone").value("13900000000"))
                .andExpect(jsonPath("$.data.qq").value("445566"))
                .andExpect(jsonPath("$.data.slogan").value("Updated slogan"));
    }

    @Test
    void loginSupportsStudentNumberAndRejectsDuplicateRegistration() throws Exception {
        getJson("/api/users/me")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));

        postJson("/api/auth/login", Map.of(
                "username", "20230001",
                "password", "123456"
        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.profile.id").value(2))
                .andExpect(jsonPath("$.data.profile.role").value("user"));

        Map<String, Object> registerRequest = new LinkedHashMap<>();
        registerRequest.put("username", "repeat-user");
        registerRequest.put("nickname", "Repeat User");
        registerRequest.put("password", "secret123");
        registerRequest.put("confirmPassword", "secret123");

        postJson("/api/auth/register", registerRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        JsonNode duplicateResponse = responseJson(postJson("/api/auth/register", registerRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400)));

        assertThat(duplicateResponse.path("message").asText()).isNotBlank();
    }
}
