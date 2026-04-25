package com.kecheng.market.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.function.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class ApiIntegrationTestSupport {

    private static final String AUTHORIZATION = "Authorization";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected String loginAndGetToken(String account) throws Exception {
        return loginAndGetToken(account, "123456");
    }

    protected String loginAndGetToken(String account, String password) throws Exception {
        ResultActions result = postJson("/api/auth/login", Map.of(
                "username", account,
                "password", password
        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        return responseData(result).path("token").asText();
    }

    protected ResultActions getJson(String url) throws Exception {
        return mockMvc.perform(get(url).accept(MediaType.APPLICATION_JSON));
    }

    protected ResultActions getJson(String url, String token) throws Exception {
        return mockMvc.perform(get(url)
                .header(AUTHORIZATION, bearer(token))
                .accept(MediaType.APPLICATION_JSON));
    }

    protected ResultActions postJson(String url, Object body) throws Exception {
        return mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    protected ResultActions postJson(String url, String token, Object body) throws Exception {
        return mockMvc.perform(post(url)
                .header(AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    protected ResultActions postNoBody(String url, String token) throws Exception {
        return mockMvc.perform(post(url)
                .header(AUTHORIZATION, bearer(token))
                .accept(MediaType.APPLICATION_JSON));
    }

    protected ResultActions putJson(String url, String token, Object body) throws Exception {
        return mockMvc.perform(put(url)
                .header(AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    protected ResultActions putNoBody(String url, String token) throws Exception {
        return mockMvc.perform(put(url)
                .header(AUTHORIZATION, bearer(token))
                .accept(MediaType.APPLICATION_JSON));
    }

    protected ResultActions deleteJson(String url, String token) throws Exception {
        return mockMvc.perform(delete(url)
                .header(AUTHORIZATION, bearer(token))
                .accept(MediaType.APPLICATION_JSON));
    }

    protected JsonNode responseJson(ResultActions result) throws Exception {
        return objectMapper.readTree(result.andReturn().getResponse().getContentAsString());
    }

    protected JsonNode responseData(ResultActions result) throws Exception {
        return responseJson(result).path("data");
    }

    protected long responseDataId(ResultActions result) throws Exception {
        return responseData(result).path("id").asLong();
    }

    protected boolean containsNode(JsonNode arrayNode, Predicate<JsonNode> predicate) {
        if (arrayNode == null || !arrayNode.isArray()) {
            return false;
        }
        for (JsonNode item : arrayNode) {
            if (predicate.test(item)) {
                return true;
            }
        }
        return false;
    }

    protected boolean containsRecordWithId(JsonNode arrayNode, long id) {
        return containsNode(arrayNode, item -> item.path("id").asLong() == id);
    }

    protected boolean containsNotification(JsonNode arrayNode, String type, long relatedId) {
        return containsNode(arrayNode, item ->
                type.equals(item.path("type").asText()) && item.path("relatedId").asLong() == relatedId);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
