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

class AppointmentAndNotificationApiIntegrationTest extends AbstractApiIntegrationTest {

    private static final String DIGITAL_CATEGORY = "\u6570\u7801\u4ea7\u54c1";

    @Test
    void appointmentCreateAndCancelUpdatesGoodsAndNotifications() throws Exception {
        String sellerToken = loginAndGetToken("zhangsan");
        String buyerToken = loginAndGetToken("lisi");
        long sellerUnreadBefore = responseData(getJson("/api/notifications/unread-count", sellerToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200)))
                .asLong();

        long goodsId = responseDataId(postJson("/api/goods", sellerToken, Map.of(
                "title", "Desk Lamp",
                "price", new BigDecimal("35.00"),
                "originalPrice", new BigDecimal("69.00"),
                "category", DIGITAL_CATEGORY,
                "campus", "Library Area",
                "condition", "Good",
                "intro", "A bright desk lamp for study.",
                "description", "Works well and has adjustable brightness.",
                "tags", List.of("lamp", "study"),
                "imageUrls", List.of()
        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200)));

        long sellerUnreadAfterPublish = responseData(getJson("/api/notifications/unread-count", sellerToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200)))
                .asLong();
        assertThat(sellerUnreadAfterPublish).isEqualTo(sellerUnreadBefore + 1);

        postJson("/api/appointments", sellerToken, Map.of(
                "goodsId", goodsId,
                "intendedTime", "2026-05-01T10:00:00",
                "intendedLocation", "Dormitory gate",
                "remark", "I published this goods myself."
        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));

        long appointmentId = responseDataId(postJson("/api/appointments", buyerToken, Map.of(
                "goodsId", goodsId,
                "intendedTime", "2026-05-01T11:00:00",
                "intendedLocation", "Teaching building square",
                "remark", "Please keep it for me until noon."
        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("pending")));

        getJson("/api/goods/" + goodsId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("reserved"));

        JsonNode sellerNotifications = responseData(getJson("/api/notifications", sellerToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200)));
        JsonNode buyerNotifications = responseData(getJson("/api/notifications", buyerToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200)));

        assertThat(containsNotification(sellerNotifications, "appointment_apply", appointmentId)).isTrue();
        assertThat(containsNotification(buyerNotifications, "appointment_apply", appointmentId)).isTrue();

        long sellerUnreadAfterApply = responseData(getJson("/api/notifications/unread-count", sellerToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200)))
                .asLong();
        assertThat(sellerUnreadAfterApply).isEqualTo(sellerUnreadAfterPublish + 1);

        long sellerApplyNotificationId = -1L;
        for (JsonNode notification : sellerNotifications) {
            if ("appointment_apply".equals(notification.path("type").asText())
                    && notification.path("relatedId").asLong() == appointmentId) {
                sellerApplyNotificationId = notification.path("id").asLong();
                break;
            }
        }
        assertThat(sellerApplyNotificationId).isPositive();

        putNoBody("/api/notifications/" + sellerApplyNotificationId + "/read", sellerToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));

        long sellerUnreadAfterRead = responseData(getJson("/api/notifications/unread-count", sellerToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200)))
                .asLong();
        assertThat(sellerUnreadAfterRead).isEqualTo(sellerUnreadAfterPublish);

        putNoBody("/api/appointments/" + appointmentId + "/cancel", buyerToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("cancelled"));

        getJson("/api/goods/" + goodsId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("on_sale"));

        JsonNode sellerNotificationsAfterCancel = responseData(getJson("/api/notifications", sellerToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200)));

        assertThat(containsNotification(sellerNotificationsAfterCancel, "appointment_cancel", appointmentId)).isTrue();
    }
}
