package com.kecheng.market;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.JsonNode;
import com.kecheng.market.appointment.entity.AppointmentEntity;
import com.kecheng.market.appointment.mapper.AppointmentMapper;
import com.kecheng.market.announcement.entity.AnnouncementEntity;
import com.kecheng.market.announcement.mapper.AnnouncementMapper;
import com.kecheng.market.comment.entity.CommentEntity;
import com.kecheng.market.comment.mapper.CommentMapper;
import com.kecheng.market.favorite.entity.FavoriteEntity;
import com.kecheng.market.favorite.mapper.FavoriteMapper;
import com.kecheng.market.goods.entity.GoodsEntity;
import com.kecheng.market.goods.mapper.GoodsMapper;
import com.kecheng.market.notification.entity.NotificationEntity;
import com.kecheng.market.notification.mapper.NotificationMapper;
import com.kecheng.market.support.AbstractMysqlApiIntegrationTest;
import com.kecheng.market.user.entity.UserEntity;
import com.kecheng.market.user.mapper.UserMapper;
import com.kecheng.market.wanted.entity.WantedEntity;
import com.kecheng.market.wanted.mapper.WantedMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MysqlPersistenceIntegrationTest extends AbstractMysqlApiIntegrationTest {

    private static final String DIGITAL_CATEGORY = "Digital";
    private static final String BOOK_CATEGORY = "Books";

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private WantedMapper wantedMapper;

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private AnnouncementMapper announcementMapper;

    @Test
    void registerLoginAndProfileUpdatePersistInMysql() throws Exception {
        String username = "mysql_user_" + System.nanoTime();
        String password = "secret123";

        long userId = responseDataId(postJson("/api/auth/register", Map.of(
                "username", username,
                "nickname", "Mysql User",
                "password", password,
                "confirmPassword", password
        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("Mysql User")));

        String token = loginAndGetToken(username, password);

        putJson("/api/users/me", token, Map.of(
                "name", "Mysql Renamed User",
                "phone", "13900001234",
                "qq", "556677",
                "slogan", "Persisted in mysql"
        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("Mysql Renamed User"));

        UserEntity storedUser = userMapper.selectById(userId);
        assertThat(storedUser).isNotNull();
        assertThat(storedUser.getUsername()).isEqualTo(username);
        assertThat(storedUser.getRealName()).isEqualTo("Mysql Renamed User");
        assertThat(storedUser.getPhone()).isEqualTo("13900001234");
        assertThat(storedUser.getQq()).isEqualTo("556677");
        assertThat(storedUser.getSlogan()).isEqualTo("Persisted in mysql");
    }

    @Test
    void goodsFavoriteCommentAppointmentAndWantedFlowsPersistInMysql() throws Exception {
        String sellerToken = loginAndGetToken("zhangsan");
        String buyerToken = loginAndGetToken("lisi");

        long goodsId = responseDataId(postJson("/api/goods", sellerToken, Map.of(
                "title", "Mysql Flow Mouse",
                "price", new BigDecimal("88.50"),
                "originalPrice", new BigDecimal("149.00"),
                "category", DIGITAL_CATEGORY,
                "campus", "North Campus",
                "condition", "Almost new",
                "intro", "A quiet wireless mouse.",
                "description", "Created against mysql persistence.",
                "tags", List.of("wireless", "mysql"),
                "imageUrls", List.of("/uploads/goods/mysql-mouse.png")
        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("on_sale")));

        GoodsEntity storedGoods = goodsMapper.selectById(goodsId);
        assertThat(storedGoods).isNotNull();
        assertThat(storedGoods.getTitle()).isEqualTo("Mysql Flow Mouse");
        assertThat(storedGoods.getUserId()).isEqualTo(2L);

        postNoBody("/api/goods/" + goodsId + "/favorite", buyerToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));

        FavoriteEntity favorite = favoriteMapper.selectAnyByUserIdAndGoodsId(3L, goodsId);
        assertThat(favorite).isNotNull();
        assertThat(favorite.getDeleted()).isZero();
        assertThat(goodsMapper.selectById(goodsId).getFavoriteCount()).isEqualTo(1L);

        postJson("/api/comments", buyerToken, Map.of(
                "goodsId", goodsId,
                "content", "Mysql comment should be stored."
        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        long commentCount = commentMapper.selectCount(Wrappers.<CommentEntity>lambdaQuery()
                .eq(CommentEntity::getGoodsId, goodsId)
                .eq(CommentEntity::getUserId, 3L)
                .eq(CommentEntity::getContent, "Mysql comment should be stored."));
        assertThat(commentCount).isEqualTo(1L);

        long appointmentId = responseDataId(postJson("/api/appointments", buyerToken, Map.of(
                "goodsId", goodsId,
                "intendedTime", "2026-06-01T10:00:00",
                "intendedLocation", "Library Gate",
                "remark", "Mysql appointment flow."
        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("pending")));

        AppointmentEntity appointment = appointmentMapper.selectById(appointmentId);
        assertThat(appointment).isNotNull();
        assertThat(appointment.getBuyerId()).isEqualTo(3L);
        assertThat(appointment.getSellerId()).isEqualTo(2L);
        assertThat(goodsMapper.selectById(goodsId).getStatus()).isEqualTo("reserved");

        putNoBody("/api/appointments/" + appointmentId + "/cancel", buyerToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("cancelled"));

        assertThat(appointmentMapper.selectById(appointmentId).getStatus()).isEqualTo("cancelled");
        assertThat(goodsMapper.selectById(goodsId).getStatus()).isEqualTo("on_sale");

        long wantedId = responseDataId(postJson("/api/wanted", sellerToken, Map.of(
                "title", "Mysql Algorithms Book",
                "budget", "Budget around 40",
                "category", BOOK_CATEGORY,
                "campus", "Main Campus",
                "deadline", "2026-07-01",
                "intro", "Need a clean copy.",
                "description", "Wanted post persisted in mysql.",
                "tags", List.of("book", "mysql"),
                "imageUrls", List.of("/uploads/wanted/mysql-book.png")
        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("buying")));

        WantedEntity wanted = wantedMapper.selectById(wantedId);
        assertThat(wanted).isNotNull();
        assertThat(wanted.getUserId()).isEqualTo(2L);
        assertThat(wanted.getTitle()).isEqualTo("Mysql Algorithms Book");

        JsonNode sellerNotifications = responseData(getJson("/api/notifications", sellerToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200)));
        assertThat(containsNotification(sellerNotifications, "favorite", goodsId)).isTrue();
        assertThat(containsNotification(sellerNotifications, "comment", goodsId)).isTrue();
        assertThat(containsNotification(sellerNotifications, "appointment_apply", appointmentId)).isTrue();
        assertThat(containsNotification(sellerNotifications, "appointment_cancel", appointmentId)).isTrue();

        long sellerNotificationCount = notificationMapper.selectCount(Wrappers.<com.kecheng.market.notification.entity.NotificationEntity>lambdaQuery()
                .eq(com.kecheng.market.notification.entity.NotificationEntity::getUserId, 2L));
        assertThat(sellerNotificationCount).isGreaterThanOrEqualTo(4L);
    }

    @Test
    void adminAnnouncementLifecyclePersistsInMysql() throws Exception {
        String adminToken = loginAndGetToken("admin");

        long announcementId = responseDataId(postJson("/api/admin/announcements", adminToken, Map.of(
                "title", "Mysql Admin Announcement",
                "summary", "Created in mysql integration test",
                "content", "This announcement verifies mysql-backed admin write paths.",
                "level", "notice",
                "top", true,
                "published", false
        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.published").value(false)));

        putNoBody("/api/admin/announcements/" + announcementId + "/publish", adminToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.published").value(true));

        AnnouncementEntity announcement = announcementMapper.selectById(announcementId);
        assertThat(announcement).isNotNull();
        assertThat(announcement.getPublished()).isTrue();
        assertThat(announcement.getTop()).isTrue();
        assertThat(announcement.getTitle()).isEqualTo("Mysql Admin Announcement");
        assertThat(announcement.getPublishedAt()).isNotNull();
    }

    @Test
    void adminUserDisableAndRecoveryPersistInMysql() throws Exception {
        String adminToken = loginAndGetToken("admin");

        putJson("/api/admin/users/2/status", adminToken, Map.of("disabled", true))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(2))
                .andExpect(jsonPath("$.data.disabled").value(true));

        UserEntity disabledUser = userMapper.selectById(2L);
        assertThat(disabledUser).isNotNull();
        assertThat(disabledUser.getStatus()).isEqualTo("disabled");

        postJson("/api/auth/login", Map.of(
                "username", "zhangsan",
                "password", "123456"
        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));

        putJson("/api/admin/users/2/status", adminToken, Map.of("disabled", false))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(2))
                .andExpect(jsonPath("$.data.disabled").value(false));

        UserEntity recoveredUser = userMapper.selectById(2L);
        assertThat(recoveredUser).isNotNull();
        assertThat(recoveredUser.getStatus()).isEqualTo("normal");

        postJson("/api/auth/login", Map.of(
                "username", "zhangsan",
                "password", "123456"
        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    void adminGoodsModerationPersistsInMysql() throws Exception {
        String adminToken = loginAndGetToken("admin");
        String sellerToken = loginAndGetToken("zhangsan");

        long goodsId = responseDataId(postJson("/api/goods", sellerToken, Map.of(
                "title", "Mysql Admin Review Headset",
                "price", new BigDecimal("79.90"),
                "originalPrice", new BigDecimal("139.00"),
                "category", DIGITAL_CATEGORY,
                "campus", "East Campus",
                "condition", "Good",
                "intro", "Created for mysql admin moderation coverage.",
                "description", "This listing should survive admin moderation assertions.",
                "tags", List.of("mysql", "admin"),
                "imageUrls", List.of("/uploads/goods/mysql-admin-headset.png")
        )));

        putNoBody("/api/admin/goods/" + goodsId + "/off-shelf", adminToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("off_shelf"));

        GoodsEntity offShelfGoods = goodsMapper.selectById(goodsId);
        assertThat(offShelfGoods).isNotNull();
        assertThat(offShelfGoods.getStatus()).isEqualTo("off_shelf");

        putNoBody("/api/admin/goods/" + goodsId + "/relist", adminToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("on_sale"));

        GoodsEntity relistedGoods = goodsMapper.selectById(goodsId);
        assertThat(relistedGoods).isNotNull();
        assertThat(relistedGoods.getStatus()).isEqualTo("on_sale");

        deleteJson("/api/admin/goods/" + goodsId, adminToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));

        assertThat(goodsMapper.selectById(goodsId)).isNull();
    }

    @Test
    void homeAggregationEndpointUsesMysqlBackedData() throws Exception {
        String adminToken = loginAndGetToken("admin");
        String sellerToken = loginAndGetToken("zhangsan");

        long goodsId = responseDataId(postJson("/api/goods", sellerToken, Map.of(
                "title", "Mysql Home Speaker",
                "price", new BigDecimal("66.00"),
                "originalPrice", new BigDecimal("129.00"),
                "category", DIGITAL_CATEGORY,
                "campus", "Main Campus",
                "condition", "Almost new",
                "intro", "Appears in mysql home aggregation.",
                "description", "Created to verify /api/home under mysql mode.",
                "tags", List.of("home", "mysql"),
                "imageUrls", List.of("/uploads/goods/mysql-home-speaker.png")
        )));

        long wantedId = responseDataId(postJson("/api/wanted", sellerToken, Map.of(
                "title", "Mysql Home Book Wanted",
                "budget", "Budget around 60",
                "category", BOOK_CATEGORY,
                "campus", "Main Campus",
                "deadline", "2026-08-01",
                "intro", "Looking for a clean second-hand book.",
                "description", "Created to verify mysql home aggregation.",
                "tags", List.of("home", "wanted"),
                "imageUrls", List.of("/uploads/wanted/mysql-home-book.png")
        )));

        long announcementId = responseDataId(postJson("/api/admin/announcements", adminToken, Map.of(
                "title", "Mysql Home Announcement",
                "summary", "Visible on home page",
                "content", "This announcement verifies mysql-backed home aggregation.",
                "level", "notice",
                "top", false,
                "published", false
        )));

        putNoBody("/api/admin/announcements/" + announcementId + "/publish", adminToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.published").value(true));

        JsonNode homeData = responseData(getJson("/api/home")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200)));

        assertThat(homeData.path("stats").size()).isEqualTo(4);
        assertThat(homeData.path("featuredGoods").isArray()).isTrue();
        assertThat(homeData.path("hotWanted").isArray()).isTrue();
        assertThat(homeData.path("latestAnnouncements").isArray()).isTrue();
        assertThat(homeData.path("banners").isArray()).isTrue();
        assertThat(containsNode(homeData.path("featuredGoods"), item -> item.path("id").asLong() == goodsId)).isTrue();
        assertThat(containsNode(homeData.path("hotWanted"), item -> item.path("id").asLong() == wantedId)).isTrue();
        assertThat(containsNode(homeData.path("latestAnnouncements"), item -> item.path("id").asLong() == announcementId)).isTrue();
    }

    @Test
    void markAllNotificationsReadPersistsInMysql() throws Exception {
        String sellerToken = loginAndGetToken("zhangsan");
        String buyerToken = loginAndGetToken("lisi");

        long goodsId = responseDataId(postJson("/api/goods", sellerToken, Map.of(
                "title", "Mysql Notification Keyboard",
                "price", new BigDecimal("59.00"),
                "originalPrice", new BigDecimal("119.00"),
                "category", DIGITAL_CATEGORY,
                "campus", "North Campus",
                "condition", "Good",
                "intro", "Used to verify notification read-all persistence.",
                "description", "This listing exists only for notification integration coverage.",
                "tags", List.of("notification", "mysql"),
                "imageUrls", List.of("/uploads/goods/mysql-notification-keyboard.png")
        )));

        postNoBody("/api/goods/" + goodsId + "/favorite", buyerToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        postJson("/api/comments", buyerToken, Map.of(
                "goodsId", goodsId,
                "content", "Trigger unread notification coverage."
        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        long unreadBefore = responseData(getJson("/api/notifications/unread-count", sellerToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200)))
                .asLong();
        assertThat(unreadBefore).isGreaterThan(0L);

        List<NotificationEntity> unreadNotifications = notificationMapper.selectList(Wrappers.<NotificationEntity>lambdaQuery()
                .eq(NotificationEntity::getUserId, 2L)
                .eq(NotificationEntity::getIsRead, Boolean.FALSE));
        Set<Long> unreadIds = unreadNotifications.stream()
                .map(NotificationEntity::getId)
                .collect(Collectors.toSet());
        assertThat(unreadIds).isNotEmpty();

        putNoBody("/api/notifications/read-all", sellerToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));

        long unreadAfter = responseData(getJson("/api/notifications/unread-count", sellerToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200)))
                .asLong();
        assertThat(unreadAfter).isZero();

        long unreadCountInDb = notificationMapper.selectCount(Wrappers.<NotificationEntity>lambdaQuery()
                .eq(NotificationEntity::getUserId, 2L)
                .eq(NotificationEntity::getIsRead, Boolean.FALSE));
        assertThat(unreadCountInDb).isZero();

        List<NotificationEntity> markedNotifications = notificationMapper.selectBatchIds(unreadIds);
        assertThat(markedNotifications).isNotEmpty();
        assertThat(markedNotifications).allSatisfy(item -> assertThat(item.getIsRead()).isTrue());
    }
}
