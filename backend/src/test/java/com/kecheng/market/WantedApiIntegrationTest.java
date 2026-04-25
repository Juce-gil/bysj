package com.kecheng.market;

import com.fasterxml.jackson.databind.JsonNode;
import com.kecheng.market.support.AbstractApiIntegrationTest;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WantedApiIntegrationTest extends AbstractApiIntegrationTest {

    private static final String BOOK_CATEGORY = "\u4e66\u7c4d\u6559\u6750";

    @Test
    void wantedDetailShowsContactOnlyToLoggedInUsers() throws Exception {
        getJson("/api/wanted/1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.contactVisible").value(false))
                .andExpect(jsonPath("$.data.phone").value(""))
                .andExpect(jsonPath("$.data.qq").value(""));

        String token = loginAndGetToken("zhangsan");
        getJson("/api/wanted/1", token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.contactVisible").value(true))
                .andExpect(jsonPath("$.data.phone").isNotEmpty());
    }

    @Test
    void wantedCreateUpdateCloseReopenAndDeleteFlowWorks() throws Exception {
        String token = loginAndGetToken("zhangsan");

        long wantedId = responseDataId(postJson("/api/wanted", token, Map.of(
                "title", "Looking for an algorithms textbook",
                "budget", "Budget around 35",
                "category", BOOK_CATEGORY,
                "campus", "Main Campus",
                "deadline", "2026-06-30",
                "intro", "Need a clean copy for exam review.",
                "description", "Prefer a complete copy with no missing pages and light notes only.",
                "tags", List.of("book", "exam"),
                "imageUrls", List.of()
        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("buying")));

        putJson("/api/wanted/" + wantedId, token, Map.of(
                "title", "Looking for an algorithms textbook second hand",
                "budget", "Budget around 40",
                "category", BOOK_CATEGORY,
                "campus", "Main Campus",
                "deadline", "2026-07-05",
                "intro", "Need a clean copy quickly.",
                "description", "Updated description with a slightly higher budget.",
                "tags", List.of("book", "urgent"),
                "imageUrls", List.of("/uploads/wanted/book-1.png")
        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("Looking for an algorithms textbook second hand"));

        putNoBody("/api/wanted/" + wantedId + "/close", token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("closed"));

        putNoBody("/api/wanted/" + wantedId + "/reopen", token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("buying"));

        deleteJson("/api/wanted/" + wantedId, token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));

        JsonNode myWanted = responseData(getJson("/api/users/me/wanted", token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200)));

        assertThat(containsRecordWithId(myWanted, wantedId)).isFalse();
    }
}
