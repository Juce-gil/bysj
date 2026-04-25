package com.kecheng.market;

import com.fasterxml.jackson.databind.JsonNode;
import com.kecheng.market.support.AbstractApiIntegrationTest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminApiIntegrationTest extends AbstractApiIntegrationTest {

    private static final String DIGITAL_CATEGORY = "\u6570\u7801\u4ea7\u54c1";

    @Test
    void adminUserManagementHonorsPermissionsAndDisabledState() throws Exception {
        String adminToken = loginAndGetToken("admin");
        String userToken = loginAndGetToken("zhangsan");

        getJson("/api/admin/users?pageNum=1&pageSize=10", userToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));

        getJson("/api/admin/users?pageNum=1&pageSize=10&keyword=zhangsan", adminToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].username").value("zhangsan"));

        putJson("/api/admin/users/2/status", adminToken, Map.of("disabled", true))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(2))
                .andExpect(jsonPath("$.data.disabled").value(true));

        postJson("/api/auth/login", Map.of(
                "username", "zhangsan",
                "password", "123456"
        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));

        putJson("/api/admin/users/1/status", adminToken, Map.of("disabled", true))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void adminCanModerateGoodsLifecycle() throws Exception {
        String adminToken = loginAndGetToken("admin");
        String sellerToken = loginAndGetToken("zhangsan");

        long goodsId = responseDataId(postJson("/api/goods", sellerToken, Map.of(
                "title", "Admin Review Mouse",
                "price", new BigDecimal("58.00"),
                "originalPrice", new BigDecimal("99.00"),
                "category", DIGITAL_CATEGORY,
                "campus", "Innovation Center",
                "condition", "Good",
                "intro", "Need admin review coverage.",
                "description", "This listing exists to verify admin moderation endpoints.",
                "tags", List.of("admin", "review"),
                "imageUrls", List.of()
        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200)));

        JsonNode goodsPage = responseData(getJson("/api/admin/goods?pageNum=1&pageSize=20", adminToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200)));

        assertThat(containsRecordWithId(goodsPage.path("records"), goodsId)).isTrue();

        putNoBody("/api/admin/goods/" + goodsId + "/off-shelf", adminToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("off_shelf"));

        putNoBody("/api/admin/goods/" + goodsId + "/relist", adminToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("on_sale"));

        deleteJson("/api/admin/goods/" + goodsId, adminToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));

        getJson("/api/admin/goods/" + goodsId, adminToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void adminAnnouncementLifecycleWorks() throws Exception {
        String adminToken = loginAndGetToken("admin");

        long announcementId = responseDataId(postJson("/api/admin/announcements", adminToken, Map.of(
                "title", "Test Admin Announcement",
                "summary", "Created by integration test",
                "content", "This announcement covers create, publish, offline and delete.",
                "level", "notice",
                "top", true,
                "published", false
        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.published").value(false)));

        JsonNode announcementPage = responseData(getJson("/api/admin/announcements?pageNum=1&pageSize=20", adminToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200)));

        assertThat(containsRecordWithId(announcementPage.path("records"), announcementId)).isTrue();

        putNoBody("/api/admin/announcements/" + announcementId + "/publish", adminToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.published").value(true))
                .andExpect(jsonPath("$.data.publishedAt").isNotEmpty());

        putNoBody("/api/admin/announcements/" + announcementId + "/offline", adminToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.published").value(false));

        deleteJson("/api/admin/announcements/" + announcementId, adminToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));

        getJson("/api/admin/announcements/" + announcementId, adminToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }
}
