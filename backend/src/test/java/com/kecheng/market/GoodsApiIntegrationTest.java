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

class GoodsApiIntegrationTest extends AbstractApiIntegrationTest {

    private static final String DIGITAL_CATEGORY = "\u6570\u7801\u4ea7\u54c1";

    @Test
    void goodsPublishEditFavoriteCommentAndNotificationFlowWorks() throws Exception {
        String sellerToken = loginAndGetToken("zhangsan");
        String buyerToken = loginAndGetToken("lisi");

        long goodsId = responseDataId(postJson("/api/goods", sellerToken, Map.of(
                "title", "Portable Mouse",
                "price", new BigDecimal("88.50"),
                "originalPrice", new BigDecimal("149.00"),
                "category", DIGITAL_CATEGORY,
                "campus", "North Campus",
                "condition", "Almost new",
                "intro", "A quiet wireless mouse.",
                "description", "Used lightly for one semester and still works perfectly.",
                "tags", List.of("wireless", "quiet"),
                "imageUrls", List.of("/uploads/goods/mouse-1.png")
        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.sellerId").value(2))
                .andExpect(jsonPath("$.data.status").value("on_sale")));

        putJson("/api/goods/" + goodsId, sellerToken, Map.of(
                "title", "Portable Mouse Pro",
                "price", new BigDecimal("79.00"),
                "originalPrice", new BigDecimal("149.00"),
                "category", DIGITAL_CATEGORY,
                "campus", "North Campus",
                "condition", "Almost new",
                "intro", "A quieter wireless mouse.",
                "description", "Updated description for the same mouse listing.",
                "tags", List.of("wireless", "bluetooth"),
                "imageUrls", List.of("/uploads/goods/mouse-1.png", "/uploads/goods/mouse-2.png")
        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("Portable Mouse Pro"))
                .andExpect(jsonPath("$.data.price").value(79.00));

        postNoBody("/api/goods/" + goodsId + "/favorite", buyerToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));

        JsonNode favoriteGoods = responseData(getJson("/api/goods/favorites/me", buyerToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200)));

        assertThat(containsRecordWithId(favoriteGoods, goodsId)).isTrue();

        postJson("/api/comments", buyerToken, Map.of(
                "goodsId", goodsId,
                "content", "Interested. Can you trade this weekend?"
        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.goodsId").value(goodsId))
                .andExpect(jsonPath("$.data.userId").value(3));

        getJson("/api/comments?goodsId=" + goodsId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].content").value("Interested. Can you trade this weekend?"));

        JsonNode sellerNotifications = responseData(getJson("/api/notifications", sellerToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200)));

        assertThat(containsNotification(sellerNotifications, "favorite", goodsId)).isTrue();
        assertThat(containsNotification(sellerNotifications, "comment", goodsId)).isTrue();
    }
}
