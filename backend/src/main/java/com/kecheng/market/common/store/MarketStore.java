/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.annotation.PostConstruct
 *  org.springframework.beans.factory.annotation.Value
 *  org.springframework.stereotype.Component
 *  org.springframework.util.StringUtils
 */
package com.kecheng.market.common.store;

import com.kecheng.market.admin.vo.AdminAnnouncementDetailVo;
import com.kecheng.market.admin.vo.AdminAnnouncementListItemVo;
import com.kecheng.market.admin.vo.AdminGoodsDetailVo;
import com.kecheng.market.admin.vo.AdminGoodsListItemVo;
import com.kecheng.market.admin.vo.AdminUserListItemVo;
import com.kecheng.market.announcement.vo.AnnouncementVo;
import com.kecheng.market.appointment.vo.AppointmentVo;
import com.kecheng.market.banner.vo.BannerVo;
import com.kecheng.market.category.vo.CategoryVo;
import com.kecheng.market.comment.vo.CommentVo;
import com.kecheng.market.common.PageResult;
import com.kecheng.market.common.exception.BusinessException;
import com.kecheng.market.common.exception.NotFoundException;
import com.kecheng.market.common.util.DateTimeUtil;
import com.kecheng.market.goods.vo.GoodsVo;
import com.kecheng.market.home.vo.HomeDataVo;
import com.kecheng.market.home.vo.HomeStatVo;
import com.kecheng.market.notification.vo.NotificationVo;
import com.kecheng.market.user.vo.UserProfileVo;
import com.kecheng.market.wanted.vo.WantedVo;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class MarketStore {
    @Value(value="${market.school-name:\u79d1\u6210}")
    private String schoolName;
    private final Map<Long, UserData> users = new ConcurrentHashMap<Long, UserData>();
    private final Map<Long, CategoryData> categories = new LinkedHashMap<Long, CategoryData>();
    private final Map<Long, GoodsData> goods = new LinkedHashMap<Long, GoodsData>();
    private final Map<Long, WantedData> wantedMap = new LinkedHashMap<Long, WantedData>();
    private final Map<Long, AnnouncementData> announcements = new LinkedHashMap<Long, AnnouncementData>();
    private final Map<Long, BannerData> banners = new LinkedHashMap<Long, BannerData>();
    private final Map<Long, List<CommentData>> commentsByGoods = new HashMap<Long, List<CommentData>>();
    private final Map<Long, LinkedHashSet<Long>> favoritesByUser = new HashMap<Long, LinkedHashSet<Long>>();
    private final Map<Long, AppointmentData> appointments = new LinkedHashMap<Long, AppointmentData>();
    private final Map<Long, List<NotificationData>> notificationsByUser = new HashMap<Long, List<NotificationData>>();
    private final AtomicLong userIdGenerator = new AtomicLong(10L);
    private final AtomicLong goodsIdGenerator = new AtomicLong(0L);
    private final AtomicLong wantedIdGenerator = new AtomicLong(0L);
    private final AtomicLong announcementIdGenerator = new AtomicLong(0L);
    private final AtomicLong commentIdGenerator = new AtomicLong(10L);
    private final AtomicLong appointmentIdGenerator = new AtomicLong(10L);
    private final AtomicLong notificationIdGenerator = new AtomicLong(20L);

    @PostConstruct
    public void init() {
        this.initUsers();
        this.initCategories();
        this.initGoods();
        this.initWanted();
        this.initAnnouncements();
        this.initBanners();
        this.initComments();
        this.initFavorites();
        this.initAppointments();
        this.initNotifications();
    }

    public UserData findUserByUsernameOrStudentNo(String account) {
        return this.users.values().stream().filter(item -> !item.deleted && (item.username.equalsIgnoreCase(account) || item.studentNo.equalsIgnoreCase(account))).findFirst().orElseThrow(() -> new BusinessException(400, "\u8d26\u53f7\u6216\u5bc6\u7801\u9519\u8bef"));
    }

    public UserProfileVo getUserProfile(Long userId) {
        UserData userData = this.getUserData(userId);
        return new UserProfileVo(userData.id, userData.name, userData.role, userData.school, userData.slogan, userData.phone, userData.qq);
    }

    public UserProfileVo updateUserProfile(Long userId, String name, String phone, String qq, String slogan) {
        UserData currentUser = this.getUserData(userId);
        String normalizedName = this.requireText(name, "Name cannot be blank");
        String normalizedPhone = this.requireText(phone, "Phone cannot be blank");
        String normalizedQq = this.normalizeOptionalText(qq);
        String normalizedSlogan = this.normalizeOptionalText(slogan);
        boolean phoneExists = this.users.values().stream().filter(item -> !item.deleted && !Objects.equals(item.id, userId)).anyMatch(item -> item.phone.equals(normalizedPhone));
        if (phoneExists) {
            throw new BusinessException(400, "Phone already exists");
        }
        UserData updatedUser = new UserData(currentUser.id, currentUser.username, currentUser.password, normalizedName, currentUser.role, currentUser.school, normalizedSlogan, currentUser.studentNo, normalizedPhone, normalizedQq, currentUser.disabled, currentUser.deleted, currentUser.createTime);
        this.users.put(userId, updatedUser);
        this.syncUserRelatedData(updatedUser);
        return this.getUserProfile(userId);
    }

    public UserProfileVo registerUser(String username, String password, String displayName, String studentNo, String phone) {
        boolean exists = this.users.values().stream().anyMatch(item -> !item.deleted && (item.username.equalsIgnoreCase(username) || item.studentNo.equalsIgnoreCase(studentNo) || item.phone.equals(phone)));
        if (exists) {
            throw new BusinessException(400, "Username, student number or phone already exists");
        }
        long id = this.userIdGenerator.incrementAndGet();
        String nickname = displayName == null || displayName.isBlank() ? username : displayName;
        this.users.put(id, new UserData(id, username, password, nickname, "user", this.schoolName, "Hope every idle item finds a new owner.", studentNo, phone, "", false, false, LocalDateTime.now()));
        return this.getUserProfile(id);
    }

    public List<CategoryVo> listCategories() {
        return this.categories.values().stream().filter(item -> !item.deleted && item.enabled).sorted(Comparator.comparingInt(item -> item.sortNum)).map(item -> new CategoryVo(item.id, item.name, item.sortNum)).toList();
    }

    public List<GoodsVo> listGoods(Long currentUserId) {
        return this.goods.values().stream()
                .filter(item -> !item.deleted)
                .sorted(Comparator.comparingLong((GoodsData item) -> item.favoriteCount)
                        .reversed()
                        .thenComparing(item -> item.id))
                .map(item -> this.toGoodsVo(item, currentUserId))
                .toList();
    }

    public List<GoodsVo> listMyGoods(Long userId) {
        return this.goods.values().stream()
                .filter(item -> !item.deleted && Objects.equals(item.sellerId, userId))
                .sorted(Comparator.comparing((GoodsData item) -> item.publishedAt)
                        .reversed()
                        .thenComparing(item -> item.id, Comparator.reverseOrder()))
                .map(item -> this.toGoodsVo(item, userId))
                .toList();
    }

    public GoodsVo getGoodsDetail(Long id, Long currentUserId) {
        GoodsData goodsData = this.goods.get(id);
        if (goodsData == null || goodsData.deleted) {
            throw new NotFoundException("\u5546\u54c1\u4e0d\u5b58\u5728");
        }
        return this.toGoodsVo(goodsData, currentUserId);
    }

    public boolean toggleFavorite(Long userId, Long goodsId) {
        boolean nowFavorited;
        GoodsData goodsData = this.getGoodsData(goodsId);
        LinkedHashSet favorites = this.favoritesByUser.computeIfAbsent(userId, key -> new LinkedHashSet());
        if (favorites.contains(goodsId)) {
            favorites.remove(goodsId);
            goodsData.favoriteCount = Math.max(0L, goodsData.favoriteCount - 1L);
            nowFavorited = false;
        } else {
            favorites.add(goodsId);
            ++goodsData.favoriteCount;
            nowFavorited = true;
            this.addNotification(goodsData.sellerId, "\u5546\u54c1\u88ab\u6536\u85cf", "\u4f60\u7684\u5546\u54c1\u300a" + goodsData.title + "\u300b\u6536\u5230\u65b0\u7684\u6536\u85cf\u3002", "favorite", goodsData.id);
        }
        return nowFavorited;
    }

    public List<GoodsVo> listMyFavorites(Long userId) {
        Set<Long> favoriteIds = this.favoritesByUser.getOrDefault(userId, new LinkedHashSet<>());
        return favoriteIds.stream()
                .map(this.goods::get)
                .filter(Objects::nonNull)
                .filter(item -> !item.deleted)
                .map(item -> this.toGoodsVo(item, userId))
                .toList();
    }

    public List<CommentVo> listComments(Long goodsId) {
        return this.commentsByGoods.getOrDefault(goodsId, List.of()).stream().sorted(Comparator.comparing(item -> item.createTime)).map(item -> new CommentVo(item.id, item.goodsId, item.userId, item.userName, item.content, DateTimeUtil.formatDateTime(item.createTime))).toList();
    }

    public CommentVo addComment(Long goodsId, Long userId, String content) {
        GoodsData goodsData = this.getGoodsData(goodsId);
        UserData userData = this.getUserData(userId);
        CommentData commentData = new CommentData(this.commentIdGenerator.incrementAndGet(), goodsId, userId, userData.name, content, LocalDateTime.now());
        this.commentsByGoods.computeIfAbsent(goodsId, key -> new ArrayList()).add(commentData);
        this.addNotification(goodsData.sellerId, "\u5546\u54c1\u6536\u5230\u65b0\u8bc4\u8bba", userData.name + " \u8bc4\u8bba\u4e86\u4f60\u7684\u5546\u54c1\u300a" + goodsData.title + "\u300b\u3002", "comment", goodsId);
        return new CommentVo(commentData.id, commentData.goodsId, commentData.userId, commentData.userName, commentData.content, DateTimeUtil.formatDateTime(commentData.createTime));
    }

    public List<WantedVo> listWanted(Long currentUserId) {
        return this.wantedMap.values().stream()
                .filter(item -> !item.deleted)
                .map(item -> this.toWantedVo(item, currentUserId != null))
                .toList();
    }

    public List<WantedVo> listMyWanted(Long userId) {
        return this.wantedMap.values().stream()
                .filter(item -> !item.deleted && Objects.equals(item.publisherId, userId))
                .sorted(Comparator.comparing((WantedData item) -> item.deadline)
                        .reversed()
                        .thenComparing(item -> item.id, Comparator.reverseOrder()))
                .map(item -> this.toWantedVo(item, true))
                .toList();
    }

    public WantedVo getWantedDetail(Long id, boolean loggedIn) {
        WantedData wantedData = this.wantedMap.get(id);
        if (wantedData == null || wantedData.deleted) {
            throw new NotFoundException("\u6c42\u8d2d\u4fe1\u606f\u4e0d\u5b58\u5728");
        }
        return this.toWantedVo(wantedData, loggedIn);
    }

    public GoodsVo createGoods(Long userId, String title, BigDecimal price, BigDecimal originalPrice, String category, String campus,
                               String condition, String intro, String description, List<String> tags, List<String> imageUrls) {
        UserData seller = this.getUserData(userId);
        String normalizedCategory = this.requireEnabledCategory(category);
        BigDecimal normalizedPrice = this.requirePositivePrice(price, "Price must be greater than 0");
        BigDecimal normalizedOriginalPrice = this.normalizeOriginalPrice(originalPrice);
        List<String> normalizedImageUrls = this.normalizeImageUrls(imageUrls);
        Long goodsId = this.goodsIdGenerator.incrementAndGet();
        GoodsData goodsData = new GoodsData(
                goodsId,
                seller.id,
                this.requireText(title, "Title cannot be blank"),
                normalizedPrice,
                normalizedOriginalPrice,
                normalizedCategory,
                this.requireText(campus, "Campus cannot be blank"),
                this.requireText(condition, "Condition cannot be blank"),
                seller.name,
                LocalDateTime.now(),
                this.requireText(intro, "Intro cannot be blank"),
                this.requireText(description, "Description cannot be blank"),
                this.normalizeTags(tags),
                normalizedImageUrls,
                this.generateCoverStyle(normalizedCategory, title),
                0L,
                "on_sale",
                false
        );
        this.goods.put(goodsId, goodsData);
        this.addNotification(userId, "Goods published", "Your goods [" + goodsData.title + "] is now visible in the marketplace.", "goods_publish", goodsId);
        return this.toGoodsVo(goodsData, userId);
    }

    public WantedVo createWanted(Long userId, String title, String budget, String category, String campus, String deadline,
                                 String intro, String description, List<String> tags, List<String> imageUrls) {
        UserData publisher = this.getUserData(userId);
        String normalizedCategory = this.requireEnabledCategory(category);
        List<String> normalizedImageUrls = this.normalizeImageUrls(imageUrls);
        Long wantedId = this.wantedIdGenerator.incrementAndGet();
        WantedData wantedData = new WantedData(
                wantedId,
                publisher.id,
                this.requireText(title, "Title cannot be blank"),
                this.requireText(budget, "Budget cannot be blank"),
                normalizedCategory,
                this.requireText(campus, "Campus cannot be blank"),
                publisher.name,
                this.parseWantedDeadline(deadline),
                this.requireText(intro, "Intro cannot be blank"),
                this.requireText(description, "Description cannot be blank"),
                this.normalizeTags(tags),
                normalizedImageUrls,
                this.generateCoverStyle(normalizedCategory, title),
                publisher.phone,
                publisher.qq,
                "buying",
                false
        );
        this.wantedMap.put(wantedId, wantedData);
        this.addNotification(userId, "Wanted published", "Your wanted post has been published successfully.", "wanted_publish", wantedId);
        return this.toWantedVo(wantedData, true);
    }

    public GoodsVo updateGoods(Long userId, Long goodsId, String title, BigDecimal price, BigDecimal originalPrice, String category,
                               String campus, String condition, String intro, String description, List<String> tags, List<String> imageUrls) {
        UserData seller = this.getUserData(userId);
        GoodsData current = this.getOwnedGoodsData(userId, goodsId);
        if ("reserved".equals(current.status) || "sold".equals(current.status)) {
            throw new BusinessException(400, "Current goods cannot be edited");
        }
        String normalizedCategory = this.requireEnabledCategory(category);
        List<String> normalizedImageUrls = this.normalizeImageUrls(imageUrls);
        GoodsData updated = new GoodsData(
                current.id,
                current.sellerId,
                this.requireText(title, "Title cannot be blank"),
                this.requirePositivePrice(price, "Price must be greater than 0"),
                this.normalizeOriginalPrice(originalPrice),
                normalizedCategory,
                this.requireText(campus, "Campus cannot be blank"),
                this.requireText(condition, "Condition cannot be blank"),
                seller.name,
                current.publishedAt,
                this.requireText(intro, "Intro cannot be blank"),
                this.requireText(description, "Description cannot be blank"),
                this.normalizeTags(tags),
                normalizedImageUrls,
                this.generateCoverStyle(normalizedCategory, title),
                current.favoriteCount,
                current.status,
                false
        );
        this.goods.put(goodsId, updated);
        this.addNotification(userId, "Goods updated", "Your goods information has been updated.", "goods_update", goodsId);
        return this.toGoodsVo(updated, userId);
    }

    public GoodsVo offShelfGoods(Long userId, Long goodsId) {
        GoodsData current = this.getOwnedGoodsData(userId, goodsId);
        if ("reserved".equals(current.status) || "sold".equals(current.status)) {
            throw new BusinessException(400, "Current goods cannot be off shelf");
        }
        if ("off_shelf".equals(current.status)) {
            return this.toGoodsVo(current, userId);
        }
        GoodsData updated = this.copyGoodsWithStatus(current, "off_shelf", false);
        this.goods.put(goodsId, updated);
        this.addNotification(userId, "Goods off shelf", "Your goods has been taken off shelf.", "goods_off_shelf", goodsId);
        return this.toGoodsVo(updated, userId);
    }

    public GoodsVo relistGoods(Long userId, Long goodsId) {
        GoodsData current = this.getOwnedGoodsData(userId, goodsId);
        if ("on_sale".equals(current.status)) {
            return this.toGoodsVo(current, userId);
        }
        if (!"off_shelf".equals(current.status)) {
            throw new BusinessException(400, "Current goods cannot be relisted");
        }
        GoodsData updated = this.copyGoodsWithStatus(current, "on_sale", false);
        this.goods.put(goodsId, updated);
        this.addNotification(userId, "Goods relisted", "Your goods has been relisted.", "goods_relist", goodsId);
        return this.toGoodsVo(updated, userId);
    }

    public void deleteGoods(Long userId, Long goodsId) {
        GoodsData current = this.getOwnedGoodsData(userId, goodsId);
        if ("reserved".equals(current.status)) {
            throw new BusinessException(400, "Reserved goods cannot be deleted");
        }
        GoodsData deleted = this.copyGoodsWithStatus(current, current.status, true);
        this.goods.put(goodsId, deleted);
        this.favoritesByUser.values().forEach(item -> item.remove(goodsId));
        this.addNotification(userId, "Goods deleted", "Your goods has been removed from the marketplace.", "goods_delete", goodsId);
    }

    public WantedVo updateWanted(Long userId, Long wantedId, String title, String budget, String category, String campus, String deadline,
                                 String intro, String description, List<String> tags, List<String> imageUrls) {
        UserData publisher = this.getUserData(userId);
        WantedData current = this.getOwnedWantedData(userId, wantedId);
        if ("finished".equals(current.status)) {
            throw new BusinessException(400, "Finished wanted post cannot be edited");
        }
        String normalizedCategory = this.requireEnabledCategory(category);
        List<String> normalizedImageUrls = this.normalizeImageUrls(imageUrls);
        WantedData updated = new WantedData(
                current.id,
                current.publisherId,
                this.requireText(title, "Title cannot be blank"),
                this.requireText(budget, "Budget cannot be blank"),
                normalizedCategory,
                this.requireText(campus, "Campus cannot be blank"),
                publisher.name,
                this.parseWantedDeadline(deadline),
                this.requireText(intro, "Intro cannot be blank"),
                this.requireText(description, "Description cannot be blank"),
                this.normalizeTags(tags),
                normalizedImageUrls,
                this.generateCoverStyle(normalizedCategory, title),
                publisher.phone,
                publisher.qq,
                current.status,
                false
        );
        this.wantedMap.put(wantedId, updated);
        this.addNotification(userId, "Wanted updated", "Your wanted post has been updated.", "wanted_update", wantedId);
        return this.toWantedVo(updated, true);
    }

    public WantedVo closeWanted(Long userId, Long wantedId) {
        WantedData current = this.getOwnedWantedData(userId, wantedId);
        if ("closed".equals(current.status)) {
            return this.toWantedVo(current, true);
        }
        if (!"buying".equals(current.status)) {
            throw new BusinessException(400, "Current wanted post cannot be closed");
        }
        WantedData updated = this.copyWantedWithStatus(current, "closed", false);
        this.wantedMap.put(wantedId, updated);
        this.addNotification(userId, "Wanted closed", "Your wanted post has been closed.", "wanted_close", wantedId);
        return this.toWantedVo(updated, true);
    }

    public WantedVo reopenWanted(Long userId, Long wantedId) {
        WantedData current = this.getOwnedWantedData(userId, wantedId);
        if ("buying".equals(current.status)) {
            return this.toWantedVo(current, true);
        }
        if (!"closed".equals(current.status)) {
            throw new BusinessException(400, "Current wanted post cannot be reopened");
        }
        WantedData updated = this.copyWantedWithStatus(current, "buying", false);
        this.wantedMap.put(wantedId, updated);
        this.addNotification(userId, "Wanted reopened", "Your wanted post has been reopened.", "wanted_reopen", wantedId);
        return this.toWantedVo(updated, true);
    }

    public void deleteWanted(Long userId, Long wantedId) {
        WantedData current = this.getOwnedWantedData(userId, wantedId);
        WantedData deleted = this.copyWantedWithStatus(current, current.status, true);
        this.wantedMap.put(wantedId, deleted);
        this.addNotification(userId, "Wanted deleted", "Your wanted post has been removed.", "wanted_delete", wantedId);
    }

    public List<AnnouncementVo> listAnnouncements() {
        return this.announcements.values().stream()
                .filter(item -> !item.deleted && item.published)
                .sorted(Comparator.comparing((AnnouncementData item) -> item.top)
                        .reversed()
                        .thenComparing(item -> item.id))
                .map(this::toAnnouncementVo)
                .toList();
    }

    public AnnouncementVo getAnnouncementDetail(Long id) {
        AnnouncementData data = this.announcements.get(id);
        if (data == null || data.deleted || !data.published) {
            throw new NotFoundException("\u516c\u544a\u4e0d\u5b58\u5728");
        }
        return this.toAnnouncementVo(data);
    }

    public List<BannerVo> listBanners() {
        return this.banners.values().stream().filter(item -> !item.deleted && item.enabled).sorted(Comparator.comparingInt(item -> item.sortNum)).map(item -> new BannerVo(item.id, item.title, item.imageUrl, item.jumpType, item.jumpTarget, item.sortNum)).toList();
    }

    public HomeDataVo getHomeData(Long currentUserId) {
        return new HomeDataVo(List.of(new HomeStatVo("\u5728\u552e\u95f2\u7f6e", String.format("%02d", this.goods.values().stream().filter(item -> !item.deleted && "on_sale".equals(item.status)).count())), new HomeStatVo("\u6c42\u8d2d\u610f\u5411", String.format("%02d", this.wantedMap.values().stream().filter(item -> !item.deleted && "buying".equals(item.status)).count())), new HomeStatVo("\u7ad9\u5185\u516c\u544a", String.format("%02d", this.announcements.values().stream().filter(item -> !item.deleted && item.published).count())), new HomeStatVo("\u5e73\u53f0\u5206\u7c7b", String.format("%02d", this.categories.values().stream().filter(item -> !item.deleted && item.enabled).count()))), this.listGoods(currentUserId).stream().limit(3L).toList(), this.listWanted(currentUserId).stream().limit(2L).toList(), this.listAnnouncements().stream().limit(3L).toList(), this.listBanners());
    }

    public AppointmentVo createAppointment(Long buyerId, Long goodsId, LocalDateTime intendedTime, String intendedLocation, String remark) {
        GoodsData goodsData = this.getGoodsData(goodsId);
        if (!"on_sale".equals(goodsData.status)) {
            throw new BusinessException(400, "\u5f53\u524d\u5546\u54c1\u6682\u65f6\u4e0d\u80fd\u9884\u7ea6");
        }
        if (Objects.equals(goodsData.sellerId, buyerId)) {
            throw new BusinessException(400, "\u4e0d\u80fd\u9884\u7ea6\u81ea\u5df1\u53d1\u5e03\u7684\u5546\u54c1");
        }
        UserData buyer = this.getUserData(buyerId);
        UserData seller = this.getUserData(goodsData.sellerId);
        AppointmentData appointmentData = new AppointmentData(this.appointmentIdGenerator.incrementAndGet(), goodsId, goodsData.title, buyerId, buyer.name, seller.id, seller.name, intendedTime, intendedLocation, remark, "pending", false);
        this.appointments.put(appointmentData.id, appointmentData);
        goodsData.status = "reserved";
        this.addNotification(seller.id, "\u65b0\u7684\u9884\u7ea6\u7533\u8bf7", buyer.name + " \u9884\u7ea6\u4e86\u4f60\u7684\u5546\u54c1\u300a" + goodsData.title + "\u300b\u3002", "appointment_apply", appointmentData.id);
        this.addNotification(buyer.id, "\u9884\u7ea6\u5df2\u63d0\u4ea4", "\u4f60\u5df2\u63d0\u4ea4\u5546\u54c1\u300a" + goodsData.title + "\u300b\u7684\u9884\u7ea6\u7533\u8bf7\uff0c\u8bf7\u7b49\u5f85\u5356\u5bb6\u786e\u8ba4\u3002", "appointment_apply", appointmentData.id);
        return this.toAppointmentVo(appointmentData);
    }

    public List<AppointmentVo> listMyAppointments(Long userId) {
        return this.appointments.values().stream()
                .filter(item -> !item.deleted && (Objects.equals(item.buyerId, userId) || Objects.equals(item.sellerId, userId)))
                .sorted(Comparator.comparing((AppointmentData item) -> item.id).reversed())
                .map(this::toAppointmentVo)
                .toList();
    }

    public AppointmentVo cancelAppointment(Long userId, Long appointmentId) {
        AppointmentData appointmentData = this.getAppointmentData(appointmentId);
        if (!Objects.equals(appointmentData.buyerId, userId)) {
            throw new BusinessException(403, "\u53ea\u80fd\u53d6\u6d88\u81ea\u5df1\u53d1\u8d77\u7684\u9884\u7ea6");
        }
        if (!"pending".equals(appointmentData.status) && !"agreed".equals(appointmentData.status)) {
            throw new BusinessException(400, "\u5f53\u524d\u9884\u7ea6\u72b6\u6001\u4e0d\u5141\u8bb8\u53d6\u6d88");
        }
        appointmentData.status = "cancelled";
        this.getGoodsData((Long)appointmentData.goodsId).status = "on_sale";
        this.addNotification(appointmentData.sellerId, "\u9884\u7ea6\u5df2\u53d6\u6d88", appointmentData.buyerName + " \u53d6\u6d88\u4e86\u5546\u54c1\u300a" + appointmentData.goodsTitle + "\u300b\u7684\u9884\u7ea6\u3002", "appointment_cancel", appointmentId);
        return this.toAppointmentVo(appointmentData);
    }

    public List<NotificationVo> listNotifications(Long userId) {
        return this.notificationsByUser.getOrDefault(userId, List.<NotificationData>of()).stream()
                .sorted(Comparator.comparing((NotificationData item) -> item.id).reversed())
                .map(this::toNotificationVo)
                .toList();
    }

    public long unreadNotificationCount(Long userId) {
        return this.notificationsByUser.getOrDefault(userId, List.<NotificationData>of()).stream().filter(item -> !item.read).count();
    }

    public void markNotificationRead(Long userId, Long notificationId) {
        NotificationData notificationData = this.notificationsByUser.getOrDefault(userId, List.<NotificationData>of()).stream()
                .filter(item -> Objects.equals(item.id, notificationId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("\u901a\u77e5\u4e0d\u5b58\u5728"));
        notificationData.read = true;
    }

    public PageResult<AdminUserListItemVo> pageAdminUsers(String keyword, String role, Boolean disabled, Long pageNum, Long pageSize) {
        List<AdminUserListItemVo> result = this.users.values().stream()
                .filter(item -> !item.deleted)
                .filter(item -> this.matchesKeyword(keyword, item.username, item.name, item.studentNo, item.phone))
                .filter(item -> !StringUtils.hasText(role) || item.role.equalsIgnoreCase(role))
                .filter(item -> disabled == null || item.disabled == disabled)
                .sorted(Comparator.comparing((UserData item) -> "admin".equals(item.role) ? 0 : 1)
                        .thenComparingLong(item -> item.id))
                .map(this::toAdminUserListItem)
                .toList();
        return this.toPageResult(result, pageNum, pageSize);
    }

    public AdminUserListItemVo updateAdminUserStatus(Long currentAdminId, Long targetUserId, boolean disabled) {
        if (disabled && Objects.equals(currentAdminId, targetUserId)) {
            throw new BusinessException(400, "Admin cannot disable self");
        }
        UserData userData = this.getUserData(targetUserId);
        UserData updatedUser = new UserData(userData.id, userData.username, userData.password, userData.name, userData.role, userData.school, userData.slogan, userData.studentNo, userData.phone, userData.qq, disabled, userData.deleted, userData.createTime);
        this.users.put(targetUserId, updatedUser);
        return this.toAdminUserListItem(updatedUser);
    }

    public PageResult<AdminGoodsListItemVo> pageAdminGoods(String keyword, String status, String category, Long pageNum, Long pageSize) {
        List<AdminGoodsListItemVo> result = this.goods.values().stream()
                .filter(item -> !item.deleted)
                .filter(item -> this.matchesKeyword(keyword, item.title, item.sellerName, item.category))
                .filter(item -> !StringUtils.hasText(status) || item.status.equalsIgnoreCase(status))
                .filter(item -> !StringUtils.hasText(category) || item.category.equalsIgnoreCase(category))
                .sorted(Comparator.comparing((GoodsData item) -> item.publishedAt)
                        .reversed()
                        .thenComparingLong(item -> item.id))
                .map(this::toAdminGoodsListItem)
                .toList();
        return this.toPageResult(result, pageNum, pageSize);
    }

    public AdminGoodsDetailVo getAdminGoodsDetail(Long id) {
        return this.toAdminGoodsDetailVo(this.getGoodsData(id));
    }

    public AdminGoodsDetailVo offShelfAdminGoods(Long id) {
        GoodsData current = this.getGoodsData(id);
        if ("reserved".equals(current.status) || "sold".equals(current.status)) {
            throw new BusinessException(400, "Current goods cannot be off shelf");
        }
        if ("off_shelf".equals(current.status)) {
            return this.toAdminGoodsDetailVo(current);
        }
        GoodsData updated = this.copyGoodsWithStatus(current, "off_shelf", false);
        this.goods.put(id, updated);
        this.addNotification(current.sellerId, "Goods off shelf", "Your goods [" + current.title + "] was taken off shelf by the administrator.", "admin_goods_off_shelf", id);
        return this.toAdminGoodsDetailVo(updated);
    }

    public AdminGoodsDetailVo relistAdminGoods(Long id) {
        GoodsData current = this.getGoodsData(id);
        if ("on_sale".equals(current.status)) {
            return this.toAdminGoodsDetailVo(current);
        }
        if (!"off_shelf".equals(current.status)) {
            throw new BusinessException(400, "Current goods cannot be relisted");
        }
        GoodsData updated = this.copyGoodsWithStatus(current, "on_sale", false);
        this.goods.put(id, updated);
        this.addNotification(current.sellerId, "Goods relisted", "Your goods [" + current.title + "] has been relisted by the administrator.", "admin_goods_relist", id);
        return this.toAdminGoodsDetailVo(updated);
    }

    public void deleteAdminGoods(Long id) {
        GoodsData current = this.getGoodsData(id);
        if ("reserved".equals(current.status)) {
            throw new BusinessException(400, "Reserved goods cannot be deleted");
        }
        GoodsData deleted = this.copyGoodsWithStatus(current, current.status, true);
        this.goods.put(id, deleted);
        this.favoritesByUser.values().forEach(item -> item.remove(id));
        this.addNotification(current.sellerId, "Goods deleted", "Your goods [" + current.title + "] was removed by the administrator.", "admin_goods_delete", id);
    }

    public PageResult<AdminAnnouncementListItemVo> pageAdminAnnouncements(String keyword, Boolean published, Long pageNum, Long pageSize) {
        List<AdminAnnouncementListItemVo> result = this.announcements.values().stream()
                .filter(item -> !item.deleted)
                .filter(item -> this.matchesKeyword(keyword, item.title, item.summary, item.content))
                .filter(item -> published == null || item.published == published)
                .sorted(Comparator.comparing((AnnouncementData item) -> item.top)
                        .reversed()
                        .thenComparing(item -> item.publishedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(item -> item.id, Comparator.reverseOrder()))
                .map(this::toAdminAnnouncementListItem)
                .toList();
        return this.toPageResult(result, pageNum, pageSize);
    }

    public AdminAnnouncementDetailVo getAdminAnnouncementDetail(Long id) {
        return this.toAdminAnnouncementDetailVo(this.getAdminAnnouncementData(id));
    }

    public AdminAnnouncementDetailVo createAdminAnnouncement(String title, String summary, String content, String level, Boolean top, Boolean published) {
        Long announcementId = this.announcementIdGenerator.incrementAndGet();
        AnnouncementData announcementData = new AnnouncementData(announcementId, this.requireText(title, "Title cannot be blank"), this.normalizeOptionalText(summary), this.requireText(content, "Content cannot be blank"), Boolean.TRUE.equals(published) ? LocalDateTime.now() : null, this.normalizeAnnouncementLevel(level), Boolean.TRUE.equals(top), Boolean.TRUE.equals(published), false);
        this.announcements.put(announcementId, announcementData);
        return this.toAdminAnnouncementDetailVo(announcementData);
    }

    public AdminAnnouncementDetailVo updateAdminAnnouncement(Long id, String title, String summary, String content, String level, Boolean top, Boolean published) {
        AnnouncementData current = this.getAdminAnnouncementData(id);
        boolean nextPublished = Boolean.TRUE.equals(published);
        LocalDateTime publishedAt = current.publishedAt;
        if (nextPublished && !current.published) {
            publishedAt = LocalDateTime.now();
        } else if (nextPublished && publishedAt == null) {
            publishedAt = LocalDateTime.now();
        }
        AnnouncementData updated = new AnnouncementData(current.id, this.requireText(title, "Title cannot be blank"), this.normalizeOptionalText(summary), this.requireText(content, "Content cannot be blank"), publishedAt, this.normalizeAnnouncementLevel(level), Boolean.TRUE.equals(top), nextPublished, false);
        this.announcements.put(id, updated);
        return this.toAdminAnnouncementDetailVo(updated);
    }

    public AdminAnnouncementDetailVo publishAdminAnnouncement(Long id) {
        AnnouncementData current = this.getAdminAnnouncementData(id);
        AnnouncementData updated = new AnnouncementData(current.id, current.title, current.summary, current.content, LocalDateTime.now(), current.level, current.top, true, false);
        this.announcements.put(id, updated);
        return this.toAdminAnnouncementDetailVo(updated);
    }

    public AdminAnnouncementDetailVo offlineAdminAnnouncement(Long id) {
        AnnouncementData current = this.getAdminAnnouncementData(id);
        AnnouncementData updated = new AnnouncementData(current.id, current.title, current.summary, current.content, current.publishedAt, current.level, current.top, false, false);
        this.announcements.put(id, updated);
        return this.toAdminAnnouncementDetailVo(updated);
    }

    public void deleteAdminAnnouncement(Long id) {
        AnnouncementData current = this.getAdminAnnouncementData(id);
        this.announcements.put(id, new AnnouncementData(current.id, current.title, current.summary, current.content, current.publishedAt, current.level, current.top, current.published, true));
    }

    private <T> PageResult<T> toPageResult(List<T> allRecords, Long pageNum, Long pageSize) {
        long safePageNum = pageNum == null || pageNum < 1L ? 1L : pageNum;
        long safePageSize = pageSize == null || pageSize < 1L ? 10L : pageSize;
        int fromIndex = (int)Math.min((safePageNum - 1L) * safePageSize, (long)allRecords.size());
        int toIndex = (int)Math.min((long)fromIndex + safePageSize, (long)allRecords.size());
        return new PageResult<T>(allRecords.subList(fromIndex, toIndex), allRecords.size(), safePageNum, safePageSize);
    }

    private boolean matchesKeyword(String keyword, String ... values) {
        if (!StringUtils.hasText((String)keyword)) {
            return true;
        }
        String normalizedKeyword = keyword.trim().toLowerCase();
        return Arrays.stream(values).filter(Objects::nonNull).map(item -> item.toLowerCase()).anyMatch(item -> item.contains(normalizedKeyword));
    }

    private String requireText(String value, String message) {
        String normalized = this.normalizeOptionalText(value);
        if (!StringUtils.hasText((String)normalized)) {
            throw new BusinessException(400, message);
        }
        return normalized;
    }

    private String normalizeOptionalText(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeAnnouncementLevel(String level) {
        return StringUtils.hasText(level) ? level.trim() : "notice";
    }

    private String requireEnabledCategory(String category) {
        String normalizedCategory = this.requireText(category, "Category cannot be blank");
        boolean exists = this.categories.values().stream()
                .anyMatch(item -> !item.deleted && item.enabled && item.name.equalsIgnoreCase(normalizedCategory));
        if (!exists) {
            throw new BusinessException(400, "Category does not exist");
        }
        return this.categories.values().stream()
                .filter(item -> !item.deleted && item.enabled && item.name.equalsIgnoreCase(normalizedCategory))
                .map(item -> item.name)
                .findFirst()
                .orElse(normalizedCategory);
    }

    private BigDecimal requirePositivePrice(BigDecimal value, String message) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(400, message);
        }
        return value.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private BigDecimal normalizeOriginalPrice(BigDecimal value) {
        if (value == null) {
            return null;
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(400, "Original price must be greater than or equal to 0");
        }
        return value.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private LocalDateTime parseWantedDeadline(String deadline) {
        String normalizedDeadline = this.requireText(deadline, "Deadline cannot be blank");
        try {
            return LocalDate.parse(normalizedDeadline).atStartOfDay();
        } catch (DateTimeParseException ex) {
            throw new BusinessException(400, "Deadline format must be yyyy-MM-dd");
        }
    }

    private List<String> normalizeTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }
        List<String> normalized = new ArrayList<>();
        for (String tag : tags) {
            String value = this.normalizeOptionalText(tag);
            if (!StringUtils.hasText(value) || normalized.contains(value)) {
                continue;
            }
            normalized.add(value);
            if (normalized.size() >= 8) {
                break;
            }
        }
        return List.copyOf(normalized);
    }

    private List<String> normalizeImageUrls(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return List.of();
        }
        List<String> normalized = new ArrayList<>();
        for (String imageUrl : imageUrls) {
            String value = this.normalizeOptionalText(imageUrl);
            if (!StringUtils.hasText(value) || normalized.contains(value)) {
                continue;
            }
            if (!value.startsWith("/uploads/")) {
                throw new BusinessException(400, "Image url is invalid");
            }
            normalized.add(value);
            if (normalized.size() >= 6) {
                break;
            }
        }
        return List.copyOf(normalized);
    }

    private String generateCoverStyle(String category, String title) {
        List<String> palettes = List.of(
                "linear-gradient(135deg, #fff1b8 0%, #ffd666 100%)",
                "linear-gradient(135deg, #ffe58f 0%, #ffb703 100%)",
                "linear-gradient(135deg, #fff7cc 0%, #ffc53d 100%)",
                "linear-gradient(135deg, #ffe7ba 0%, #faad14 100%)",
                "linear-gradient(135deg, #fffbe6 0%, #ffd43b 100%)"
        );
        int index = Math.abs(Objects.hash(this.normalizeOptionalText(category), this.normalizeOptionalText(title))) % palettes.size();
        return palettes.get(index);
    }

    private void syncUserRelatedData(UserData userData) {
        this.goods.replaceAll((id, item) -> Objects.equals(item.sellerId, userData.id) ? new GoodsData(item.id, item.sellerId, item.title, item.price, item.originalPrice, item.category, item.campus, item.condition, userData.name, item.publishedAt, item.intro, item.description, item.tags, item.imageUrls, item.coverStyle, item.favoriteCount, item.status, item.deleted) : item);
        this.wantedMap.replaceAll((id, item) -> Objects.equals(item.publisherId, userData.id) ? new WantedData(item.id, item.publisherId, item.title, item.budget, item.category, item.campus, userData.name, item.deadline, item.intro, item.description, item.tags, item.imageUrls, item.coverStyle, userData.phone, userData.qq, item.status, item.deleted) : item);
        this.commentsByGoods.replaceAll((goodsId, items) -> items.stream().map(item -> Objects.equals(item.userId, userData.id) ? new CommentData(item.id, item.goodsId, item.userId, userData.name, item.content, item.createTime) : item).toList());
        this.appointments.replaceAll((id, item) -> {
            if (Objects.equals(item.buyerId, userData.id)) {
                return new AppointmentData(item.id, item.goodsId, item.goodsTitle, item.buyerId, userData.name, item.sellerId, item.sellerName, item.intendedTime, item.intendedLocation, item.remark, item.status, item.deleted);
            }
            if (Objects.equals(item.sellerId, userData.id)) {
                return new AppointmentData(item.id, item.goodsId, item.goodsTitle, item.buyerId, item.buyerName, item.sellerId, userData.name, item.intendedTime, item.intendedLocation, item.remark, item.status, item.deleted);
            }
            return item;
        });
    }

    private AdminUserListItemVo toAdminUserListItem(UserData item) {
        return new AdminUserListItemVo(item.id, item.username, item.name, item.role, item.studentNo, item.phone, item.school, item.disabled, DateTimeUtil.formatDateTime(item.createTime));
    }

    private AdminAnnouncementListItemVo toAdminAnnouncementListItem(AnnouncementData item) {
        return new AdminAnnouncementListItemVo(item.id, item.title, item.summary, item.level, item.top, item.published, DateTimeUtil.formatDateTime(item.publishedAt));
    }

    private AdminAnnouncementDetailVo toAdminAnnouncementDetailVo(AnnouncementData item) {
        return new AdminAnnouncementDetailVo(item.id, item.title, item.summary, item.content, item.level, item.top, item.published, DateTimeUtil.formatDateTime(item.publishedAt));
    }

    private GoodsVo toGoodsVo(GoodsData item, Long currentUserId) {
        boolean favorited = currentUserId != null && this.favoritesByUser.getOrDefault(currentUserId, new LinkedHashSet()).contains(item.id);
        String coverImageUrl = item.imageUrls.isEmpty() ? "" : item.imageUrls.get(0);
        return new GoodsVo(item.id, item.title, item.price, item.originalPrice, item.category, item.campus, item.condition, item.sellerName, DateTimeUtil.formatDateTime(item.publishedAt), item.intro, item.description, item.tags, item.imageUrls, coverImageUrl, item.coverStyle, item.favoriteCount, favorited, item.status);
    }

    private AdminGoodsListItemVo toAdminGoodsListItem(GoodsData item) {
        return new AdminGoodsListItemVo(item.id, item.title, item.price, item.category, item.sellerName, item.favoriteCount, item.status, DateTimeUtil.formatDateTime(item.publishedAt));
    }

    private AdminGoodsDetailVo toAdminGoodsDetailVo(GoodsData item) {
        return new AdminGoodsDetailVo(item.id, item.title, item.price, item.originalPrice, item.category, item.campus, item.condition, item.sellerName, item.intro, item.description, item.tags, item.favoriteCount, item.status, DateTimeUtil.formatDateTime(item.publishedAt));
    }

    private WantedVo toWantedVo(WantedData item, boolean loggedIn) {
        String coverImageUrl = item.imageUrls.isEmpty() ? "" : item.imageUrls.get(0);
        return new WantedVo(item.id, item.title, item.budget, item.category, item.campus, item.publisherName,
                DateTimeUtil.formatDate(item.deadline), item.intro, item.description, item.tags, item.imageUrls,
                coverImageUrl, item.coverStyle, loggedIn, loggedIn ? item.phone : "", loggedIn ? item.qq : "", item.status);
    }

    private AnnouncementVo toAnnouncementVo(AnnouncementData item) {
        return new AnnouncementVo(item.id, item.title, item.summary, item.content, DateTimeUtil.formatDateTime(item.publishedAt), item.level);
    }

    private AppointmentVo toAppointmentVo(AppointmentData item) {
        return new AppointmentVo(item.id, item.goodsId, item.goodsTitle, item.buyerId, item.buyerName, item.sellerId, item.sellerName, DateTimeUtil.formatDateTime(item.intendedTime), item.intendedLocation, item.remark, item.status);
    }

    private NotificationVo toNotificationVo(NotificationData item) {
        return new NotificationVo(item.id, item.title, item.content, item.type, item.read, item.relatedId, DateTimeUtil.formatDateTime(item.createTime));
    }

    private UserData getUserData(Long userId) {
        UserData userData = this.users.get(userId);
        if (userData == null || userData.deleted) {
            throw new NotFoundException("User not found");
        }
        return userData;
    }

    private GoodsData getGoodsData(Long goodsId) {
        GoodsData goodsData = this.goods.get(goodsId);
        if (goodsData == null || goodsData.deleted) {
            throw new NotFoundException("\u5546\u54c1\u4e0d\u5b58\u5728");
        }
        return goodsData;
    }

    private GoodsData getOwnedGoodsData(Long userId, Long goodsId) {
        GoodsData goodsData = this.getGoodsData(goodsId);
        if (!Objects.equals(goodsData.sellerId, userId)) {
            throw new BusinessException(403, "No permission to operate this goods");
        }
        return goodsData;
    }

    private WantedData getWantedData(Long wantedId) {
        WantedData wantedData = this.wantedMap.get(wantedId);
        if (wantedData == null || wantedData.deleted) {
            throw new NotFoundException("\u6c42\u8d2d\u4fe1\u606f\u4e0d\u5b58\u5728");
        }
        return wantedData;
    }

    private WantedData getOwnedWantedData(Long userId, Long wantedId) {
        WantedData wantedData = this.getWantedData(wantedId);
        if (!Objects.equals(wantedData.publisherId, userId)) {
            throw new BusinessException(403, "No permission to operate this wanted post");
        }
        return wantedData;
    }

    private GoodsData copyGoodsWithStatus(GoodsData current, String status, boolean deleted) {
        return new GoodsData(current.id, current.sellerId, current.title, current.price, current.originalPrice, current.category,
                current.campus, current.condition, current.sellerName, current.publishedAt, current.intro, current.description,
                current.tags, current.imageUrls, current.coverStyle, current.favoriteCount, status, deleted);
    }

    private WantedData copyWantedWithStatus(WantedData current, String status, boolean deleted) {
        return new WantedData(current.id, current.publisherId, current.title, current.budget, current.category, current.campus,
                current.publisherName, current.deadline, current.intro, current.description, current.tags, current.imageUrls,
                current.coverStyle, current.phone, current.qq, status, deleted);
    }

    private AppointmentData getAppointmentData(Long appointmentId) {
        AppointmentData appointmentData = this.appointments.get(appointmentId);
        if (appointmentData == null || appointmentData.deleted) {
            throw new NotFoundException("Appointment not found");
        }
        return appointmentData;
    }

    private AnnouncementData getAdminAnnouncementData(Long announcementId) {
        AnnouncementData announcementData = this.announcements.get(announcementId);
        if (announcementData == null || announcementData.deleted) {
            throw new NotFoundException("Announcement not found");
        }
        return announcementData;
    }

    private void addNotification(Long userId, String title, String content, String type, Long relatedId) {
        this.notificationsByUser.computeIfAbsent(userId, key -> new ArrayList()).add(new NotificationData(this.notificationIdGenerator.incrementAndGet(), title, content, type, false, relatedId, LocalDateTime.now()));
    }

    private void initUsers() {
        this.users.put(1L, new UserData(1L, "admin", "123456", "\u6821\u56ed\u7ba1\u7406\u5458", "admin", this.schoolName, "Maintain trust in every campus trade.", "ADMIN0001", "13800000000", "10000", false, false, LocalDateTime.now().minusDays(30L)));
        this.users.put(2L, new UserData(2L, "zhangsan", "123456", "\u5f20\u4e09", "user", this.schoolName, "Let every idle item be useful again.", "20230001", "13800000001", "123456789", false, false, LocalDateTime.now().minusDays(14L)));
        this.users.put(3L, new UserData(3L, "lisi", "123456", "\u674e\u56db", "user", this.schoolName, "Organize life and make more room.", "20230002", "13800000002", "987654321", false, false, LocalDateTime.now().minusDays(10L)));
        this.users.put(4L, new UserData(4L, "wangwu", "123456", "\u738b\u4e94", "user", this.schoolName, "Pass old items to the people who need them.", "20230003", "13800000003", "112233445", false, false, LocalDateTime.now().minusDays(7L)));
    }

    private void initCategories() {
        this.categories.put(1L, new CategoryData(1L, "\u6570\u7801\u4ea7\u54c1", 1, true, false));
        this.categories.put(2L, new CategoryData(2L, "\u4e66\u7c4d\u6559\u6750", 2, true, false));
        this.categories.put(3L, new CategoryData(3L, "\u65e5\u7528\u54c1", 3, true, false));
        this.categories.put(4L, new CategoryData(4L, "\u8863\u7269\u978b\u5e3d", 4, true, false));
        this.categories.put(5L, new CategoryData(5L, "\u4f53\u80b2\u7528\u54c1", 5, true, false));
        this.categories.put(6L, new CategoryData(6L, "\u5176\u4ed6", 6, true, false));
    }

    private void initGoods() {
        this.goods.put(1L, new GoodsData(1L, 2L, "\u4e5d\u6210\u65b0\u673a\u68b0\u952e\u76d8", new BigDecimal("120.00"), new BigDecimal("299.00"), "\u6570\u7801\u4ea7\u54c1", "\u79d1\u6210\u4e3b\u6821\u533a", "\u4e5d\u6210\u65b0", "\u5f20\u4e09", LocalDateTime.now().minusDays(4L), "\u81ea\u7528\u673a\u68b0\u952e\u76d8\uff0c\u6210\u8272\u8f83\u597d\uff0c\u706f\u5149\u6b63\u5e38\uff0c\u529f\u80fd\u65e0\u95ee\u9898\u3002", "\u81ea\u7528\u673a\u68b0\u952e\u76d8\uff0c\u6210\u8272\u8f83\u597d\uff0c\u706f\u5149\u6b63\u5e38\uff0c\u529f\u80fd\u65e0\u95ee\u9898\uff0c\u9002\u5408\u65e5\u5e38\u529e\u516c\u548c\u6e38\u620f\u4f7f\u7528\u3002", List.of("\u673a\u68b0\u952e\u76d8", "\u53ef\u8bae\u4ef7", "\u6821\u5185\u9762\u4ea4"), List.of(), "linear-gradient(135deg, #d7ecff 0%, #8fbdf6 100%)", 2L, "on_sale", false));
        this.goods.put(2L, new GoodsData(2L, 3L, "\u9ad8\u7b49\u6570\u5b66\u6559\u6750", new BigDecimal("25.00"), new BigDecimal("58.00"), "\u4e66\u7c4d\u6559\u6750", "\u79d1\u6210\u56fe\u4e66\u9986\u533a", "\u516b\u6210\u65b0", "\u674e\u56db", LocalDateTime.now().minusDays(3L), "\u5e38\u7528\u9ad8\u7b49\u6570\u5b66\u6559\u6750\uff0c\u5185\u5bb9\u5b8c\u6574\uff0c\u6709\u5c11\u91cf\u7b14\u8bb0\u3002", "\u79d1\u6210\u5927\u5b66\u5e38\u7528\u9ad8\u7b49\u6570\u5b66\u6559\u6750\uff0c\u5185\u5bb9\u5b8c\u6574\uff0c\u6709\u5c11\u91cf\u7b14\u8bb0\uff0c\u9002\u5408\u590d\u4e60\u5907\u8003\u4f7f\u7528\u3002", List.of("\u6559\u6750", "\u5907\u8003", "\u7b14\u8bb0\u5b8c\u6574"), List.of(), "linear-gradient(135deg, #f2f7ff 0%, #b9cbff 100%)", 1L, "reserved", false));
        this.goods.put(3L, new GoodsData(3L, 4L, "\u7fbd\u6bdb\u7403\u62cd\u4e00\u526f", new BigDecimal("60.00"), new BigDecimal("150.00"), "\u4f53\u80b2\u7528\u54c1", "\u79d1\u6210\u4f53\u80b2\u9986", "\u4e03\u6210\u65b0\u53ca\u4ee5\u4e0b", "\u738b\u4e94", LocalDateTime.now().minusDays(6L), "\u5e73\u65f6\u6253\u7403\u4f7f\u7528\uff0c\u6709\u6b63\u5e38\u78e8\u635f\uff0c\u62cd\u7ebf\u5b8c\u597d\u3002", "\u5e73\u65f6\u6253\u7403\u4f7f\u7528\uff0c\u6709\u6b63\u5e38\u78e8\u635f\uff0c\u62cd\u7ebf\u5b8c\u597d\uff0c\u53ef\u7ee7\u7eed\u4f7f\u7528\u3002", List.of("\u7fbd\u6bdb\u7403", "\u5165\u95e8\u53cb\u597d", "\u5df2\u552e\u51fa\u793a\u4f8b"), List.of(), "linear-gradient(135deg, #eaf8ff 0%, #8fd4d9 100%)", 1L, "sold", false));
        this.goods.put(4L, new GoodsData(4L, 2L, "\u5bbf\u820d\u6536\u7eb3\u7bb1", new BigDecimal("18.00"), new BigDecimal("39.00"), "\u65e5\u7528\u54c1", "\u79d1\u6210\u5bbf\u820d\u533a", "\u516b\u6210\u65b0", "\u5f20\u4e09", LocalDateTime.now().minusDays(2L), "\u900f\u660e\u6536\u7eb3\u7bb1\uff0c\u5bb9\u91cf\u8f83\u5927\uff0c\u9002\u5408\u653e\u8863\u7269\u548c\u4e66\u672c\u3002", "\u900f\u660e\u6536\u7eb3\u7bb1\uff0c\u5bb9\u91cf\u8f83\u5927\uff0c\u9002\u5408\u653e\u8863\u7269\u548c\u4e66\u672c\uff0c\u5bbf\u820d\u642c\u8fc1\u4f4e\u4ef7\u51fa\u3002", List.of("\u5bbf\u820d\u642c\u8fc1", "\u6536\u7eb3", "\u4f4e\u4ef7\u51fa"), List.of(), "linear-gradient(135deg, #eef8ff 0%, #9fd7ff 100%)", 0L, "off_shelf", false));
        this.goodsIdGenerator.set(this.goods.keySet().stream().mapToLong(Long::longValue).max().orElse(0L));
    }

    private void initWanted() {
        this.wantedMap.put(1L, new WantedData(1L, 3L, "\u6c42\u8d2d\u4e8c\u624b\u53f0\u706f", "\u9884\u7b97 30 \u5143\u5de6\u53f3", "\u65e5\u7528\u54c1", "\u79d1\u6210\u5bbf\u820d\u533a", "\u674e\u56db", LocalDateTime.now().plusDays(7L), "\u60f3\u6536\u4e00\u4e2a\u5bbf\u820d\u5b66\u4e60\u7528\u53f0\u706f\uff0c\u8981\u6c42\u53ef\u4ee5\u6b63\u5e38\u8c03\u5149\u3002", "\u60f3\u6536\u4e00\u4e2a\u5bbf\u820d\u5b66\u4e60\u7528\u53f0\u706f\uff0c\u8981\u6c42\u53ef\u4ee5\u6b63\u5e38\u8c03\u5149\uff0c\u5916\u89c2\u4e0d\u8981\u592a\u65e7\u3002", List.of("\u5bbf\u820d\u5b66\u4e60", "\u53ef\u8c03\u5149", "\u9884\u7b97\u53cb\u597d"), List.of(), "linear-gradient(135deg, #dff8ff 0%, #80c7ff 100%)", "13800000002", "987654321", "buying", false));
        this.wantedMap.put(2L, new WantedData(2L, 2L, "\u6c42\u8d2d\u56db\u516d\u7ea7\u5907\u8003\u8d44\u6599", "\u9884\u7b97 20 \u5143\u5de6\u53f3", "\u4e66\u7c4d\u6559\u6750", "\u79d1\u6210\u6559\u5b66\u533a", "\u5f20\u4e09", LocalDateTime.now().plusDays(10L), "\u60f3\u6536\u4e00\u5957\u56db\u516d\u7ea7\u5907\u8003\u8d44\u6599\uff0c\u6700\u597d\u5e26\u771f\u9898\u548c\u7b14\u8bb0\u3002", "\u60f3\u6536\u4e00\u5957\u56db\u516d\u7ea7\u5907\u8003\u8d44\u6599\uff0c\u6700\u597d\u5e26\u771f\u9898\u548c\u7b14\u8bb0\uff0c\u6210\u8272\u65e0\u6240\u8c13\uff0c\u5185\u5bb9\u5b8c\u6574\u5373\u53ef\u3002", List.of("\u56db\u516d\u7ea7", "\u771f\u9898", "\u8d44\u6599"), List.of(), "linear-gradient(135deg, #edf7ff 0%, #9cc6ff 100%)", "13800000001", "123456789", "buying", false));
        this.wantedMap.put(3L, new WantedData(3L, 4L, "\u6c42\u8d2d\u4e8c\u624b\u8fd0\u52a8\u6c34\u676f", "\u9884\u7b97 15 \u5143\u5de6\u53f3", "\u65e5\u7528\u54c1", "\u79d1\u6210\u64cd\u573a\u533a", "\u738b\u4e94", LocalDateTime.now().plusDays(4L), "\u6c42\u8d2d\u4e00\u4e2a\u5bb9\u91cf\u8f83\u5927\u7684\u8fd0\u52a8\u6c34\u676f\u3002", "\u6c42\u8d2d\u4e00\u4e2a\u5bb9\u91cf\u8f83\u5927\u7684\u8fd0\u52a8\u6c34\u676f\uff0c\u8981\u6c42\u65e0\u660e\u663e\u7834\u635f\uff0c\u4e0d\u6f0f\u6c34\u5373\u53ef\u3002", List.of("\u8fd0\u52a8", "\u6c34\u676f", "\u5b9e\u7528"), List.of(), "linear-gradient(135deg, #f9fbff 0%, #c0d8ff 100%)", "13800000003", "112233445", "closed", false));
        this.wantedIdGenerator.set(this.wantedMap.keySet().stream().mapToLong(Long::longValue).max().orElse(0L));
    }

    private void initAnnouncements() {
        this.announcements.put(1L, new AnnouncementData(1L, "\u6b22\u8fce\u4f7f\u7528\u6821\u56ed\u8df3\u86a4\u5e02\u573a", "\u6b22\u8fce\u6765\u5230\u79d1\u6210\u6821\u56ed\u8df3\u86a4\u5e02\u573a\u5e73\u53f0\u3002", "\u6b22\u8fce\u6765\u5230\u79d1\u6210\u6821\u56ed\u8df3\u86a4\u5e02\u573a\u5e73\u53f0\u3002\u672c\u5e73\u53f0\u4e3b\u8981\u9762\u5411\u6821\u56ed\u5185\u90e8\u7528\u6237\uff0c\u63d0\u4f9b\u4e8c\u624b\u5546\u54c1\u53d1\u5e03\u3001\u6c42\u8d2d\u4fe1\u606f\u53d1\u5e03\u4ee5\u53ca\u9884\u7ea6\u9762\u4ea4\u7b49\u529f\u80fd\u3002", LocalDateTime.now().minusDays(5L), "\u901a\u77e5", true, true, false));
        this.announcements.put(2L, new AnnouncementData(2L, "\u5e73\u53f0\u4f7f\u7528\u8bf4\u660e", "\u5e73\u53f0\u4ec5\u4f9b\u6821\u56ed\u5185\u90e8\u5b66\u4e60\u4e0e\u4ea4\u6d41\u4f7f\u7528\u3002", "\u672c\u5e73\u53f0\u4ec5\u4f9b\u6821\u56ed\u5185\u90e8\u5b66\u4e60\u4e0e\u4ea4\u6d41\u4f7f\u7528\uff0c\u4ea4\u6613\u65b9\u5f0f\u4e3a\u7ebf\u4e0a\u53d1\u5e03\u4fe1\u606f\u3001\u7ebf\u4e0b\u9762\u4ea4\u3002\u8bf7\u540c\u5b66\u4eec\u7406\u6027\u4ea4\u6613\uff0c\u6ce8\u610f\u6838\u9a8c\u5546\u54c1\u4fe1\u606f\u5e76\u4fdd\u62a4\u4e2a\u4eba\u8d22\u4ea7\u5b89\u5168\u3002", LocalDateTime.now().minusDays(2L), "\u63d0\u793a", false, true, false));
        this.announcements.put(3L, new AnnouncementData(3L, "\u671f\u672b\u95f2\u7f6e\u7269\u54c1\u96c6\u4e2d\u4ea4\u6613\u63d0\u9192", "\u4e34\u8fd1\u671f\u672b\uff0c\u5e73\u53f0\u5c06\u8fce\u6765\u95f2\u7f6e\u7269\u54c1\u53d1\u5e03\u9ad8\u5cf0\u3002", "\u4e34\u8fd1\u671f\u672b\uff0c\u5e73\u53f0\u5c06\u8fce\u6765\u95f2\u7f6e\u7269\u54c1\u53d1\u5e03\u9ad8\u5cf0\uff0c\u8bf7\u5927\u5bb6\u89c4\u8303\u586b\u5199\u5546\u54c1\u4fe1\u606f\uff0c\u4e0a\u4f20\u6e05\u6670\u56fe\u7247\uff0c\u63d0\u9ad8\u4ea4\u6613\u6548\u7387\u3002", LocalDateTime.now().minusHours(18L), "\u6d3b\u52a8", false, true, false));
        this.announcementIdGenerator.set(this.announcements.keySet().stream().mapToLong(Long::longValue).max().orElse(0L));
    }

    private void initBanners() {
        this.banners.put(1L, new BannerData(1L, "\u6b22\u8fce\u4f7f\u7528\u6821\u56ed\u8df3\u86a4\u5e02\u573a", "/upload/banner/banner1.jpg", "announcement", "1", 1, true, false));
        this.banners.put(2L, new BannerData(2L, "\u5e73\u53f0\u4f7f\u7528\u8bf4\u660e", "/upload/banner/banner2.jpg", "announcement", "2", 2, true, false));
        this.banners.put(3L, new BannerData(3L, "\u70ed\u95e8\u5546\u54c1\u63a8\u8350", "/upload/banner/banner3.jpg", "goods", "1", 3, true, false));
    }

    private void initComments() {
        this.commentsByGoods.put(1L, new ArrayList<CommentData>(List.of(new CommentData(1L, 1L, 3L, "\u674e\u56db", "\u8fd9\u4e2a\u952e\u76d8\u770b\u8d77\u6765\u4e0d\u9519\uff0c\u8bf7\u95ee\u652f\u6301\u5c0f\u5200\u5417\uff1f", LocalDateTime.now().minusDays(1L)), new CommentData(2L, 1L, 4L, "\u738b\u4e94", "\u60f3\u95ee\u4e00\u4e0b\u952e\u76d8\u8f74\u4f53\u662f\u4ec0\u4e48\u7c7b\u578b\uff1f", LocalDateTime.now().minusHours(12L)))));
        this.commentsByGoods.put(2L, new ArrayList<CommentData>(List.of(new CommentData(3L, 2L, 2L, "\u5f20\u4e09", "\u8fd9\u672c\u6559\u6750\u8fd8\u6709\u914d\u5957\u4e60\u9898\u518c\u5417\uff1f", LocalDateTime.now().minusHours(8L)))));
    }

    private void initFavorites() {
        this.favoritesByUser.put(3L, new LinkedHashSet<Long>(List.of(Long.valueOf(1L), Long.valueOf(3L))));
        this.favoritesByUser.put(4L, new LinkedHashSet<Long>(List.of(Long.valueOf(1L))));
        this.favoritesByUser.put(2L, new LinkedHashSet<Long>(List.of(Long.valueOf(2L))));
    }

    private void initAppointments() {
        this.appointments.put(1L, new AppointmentData(1L, 2L, "\u9ad8\u7b49\u6570\u5b66\u6559\u6750", 2L, "\u5f20\u4e09", 3L, "\u674e\u56db", LocalDateTime.now().plusDays(1L), "\u79d1\u6210\u56fe\u4e66\u9986\u95e8\u53e3", "\u60f3\u5f53\u9762\u770b\u770b\u4e66\u672c\u7b14\u8bb0\u60c5\u51b5\u3002", "pending", false));
        this.appointments.put(2L, new AppointmentData(2L, 3L, "\u7fbd\u6bdb\u7403\u62cd\u4e00\u526f", 3L, "\u674e\u56db", 4L, "\u738b\u4e94", LocalDateTime.now().minusDays(2L), "\u79d1\u6210\u4f53\u80b2\u9986\u897f\u95e8", "\u5df2\u7ecf\u6c9f\u901a\u8fc7\uff0c\u7ea6\u65f6\u95f4\u9762\u4ea4\u3002", "completed", false));
    }

    private void initNotifications() {
        this.notificationsByUser.put(3L, new ArrayList<NotificationData>(List.of(new NotificationData(1L, "\u65b0\u7684\u9884\u7ea6\u7533\u8bf7", "\u4f60\u7684\u5546\u54c1\u300a\u9ad8\u7b49\u6570\u5b66\u6559\u6750\u300b\u6536\u5230\u4e00\u6761\u65b0\u7684\u9884\u7ea6\u7533\u8bf7\uff0c\u8bf7\u53ca\u65f6\u5904\u7406\u3002", "appointment_apply", false, 1L, LocalDateTime.now().minusHours(3L)), new NotificationData(4L, "\u4ea4\u6613\u5df2\u5b8c\u6210", "\u4f60\u9884\u7ea6\u7684\u300a\u7fbd\u6bdb\u7403\u62cd\u4e00\u526f\u300b\u4ea4\u6613\u5df2\u5b8c\u6210\uff0c\u611f\u8c22\u4f7f\u7528\u6821\u56ed\u8df3\u86a4\u5e02\u573a\u3002", "appointment_complete", false, 2L, LocalDateTime.now().minusDays(1L)))));
        this.notificationsByUser.put(2L, new ArrayList<NotificationData>(List.of(new NotificationData(2L, "\u9884\u7ea6\u5df2\u63d0\u4ea4", "\u4f60\u5df2\u6210\u529f\u63d0\u4ea4\u300a\u9ad8\u7b49\u6570\u5b66\u6559\u6750\u300b\u7684\u9884\u7ea6\u7533\u8bf7\uff0c\u8bf7\u7b49\u5f85\u5356\u5bb6\u786e\u8ba4\u3002", "appointment_apply", true, 1L, LocalDateTime.now().minusHours(3L)), new NotificationData(5L, "\u7cfb\u7edf\u516c\u544a\u63d0\u9192", "\u5e73\u53f0\u5df2\u53d1\u5e03\u65b0\u7684\u4f7f\u7528\u8bf4\u660e\u516c\u544a\uff0c\u8bf7\u53ca\u65f6\u67e5\u770b\u3002", "announcement", false, 2L, LocalDateTime.now().minusHours(1L)))));
        this.notificationsByUser.put(4L, new ArrayList<NotificationData>(List.of(new NotificationData(3L, "\u4ea4\u6613\u5df2\u5b8c\u6210", "\u4f60\u7684\u5546\u54c1\u300a\u7fbd\u6bdb\u7403\u62cd\u4e00\u526f\u300b\u5bf9\u5e94\u9884\u7ea6\u5df2\u5b8c\u6210\uff0c\u5546\u54c1\u72b6\u6001\u5df2\u53d8\u66f4\u4e3a\u5df2\u552e\u51fa\u3002", "appointment_complete", true, 2L, LocalDateTime.now().minusDays(1L)))));
    }

    public static final class UserData {
        public final Long id;
        public final String username;
        public final String password;
        public final String name;
        public final String role;
        public final String school;
        public final String slogan;
        public final String studentNo;
        public final String phone;
        public final String qq;
        public final boolean disabled;
        public final boolean deleted;
        public final LocalDateTime createTime;

        public UserData(Long id, String username, String password, String name, String role, String school, String slogan, String studentNo, String phone, String qq, boolean disabled, boolean deleted, LocalDateTime createTime) {
            this.id = id;
            this.username = username;
            this.password = password;
            this.name = name;
            this.role = role;
            this.school = school;
            this.slogan = slogan;
            this.studentNo = studentNo;
            this.phone = phone;
            this.qq = qq;
            this.disabled = disabled;
            this.deleted = deleted;
            this.createTime = createTime;
        }
    }

    public static final class GoodsData {
        public final Long id;
        public final Long sellerId;
        public final String title;
        public final BigDecimal price;
        public final BigDecimal originalPrice;
        public final String category;
        public final String campus;
        public final String condition;
        public final String sellerName;
        public final LocalDateTime publishedAt;
        public final String intro;
        public final String description;
        public final List<String> tags;
        public final List<String> imageUrls;
        public final String coverStyle;
        public long favoriteCount;
        public String status;
        public final boolean deleted;

        public GoodsData(Long id, Long sellerId, String title, BigDecimal price, BigDecimal originalPrice, String category, String campus, String condition, String sellerName, LocalDateTime publishedAt, String intro, String description, List<String> tags, List<String> imageUrls, String coverStyle, long favoriteCount, String status, boolean deleted) {
            this.id = id;
            this.sellerId = sellerId;
            this.title = title;
            this.price = price;
            this.originalPrice = originalPrice;
            this.category = category;
            this.campus = campus;
            this.condition = condition;
            this.sellerName = sellerName;
            this.publishedAt = publishedAt;
            this.intro = intro;
            this.description = description;
            this.tags = tags;
            this.imageUrls = imageUrls;
            this.coverStyle = coverStyle;
            this.favoriteCount = favoriteCount;
            this.status = status;
            this.deleted = deleted;
        }
    }

    public static final class CommentData {
        public final Long id;
        public final Long goodsId;
        public final Long userId;
        public final String userName;
        public final String content;
        public final LocalDateTime createTime;

        public CommentData(Long id, Long goodsId, Long userId, String userName, String content, LocalDateTime createTime) {
            this.id = id;
            this.goodsId = goodsId;
            this.userId = userId;
            this.userName = userName;
            this.content = content;
            this.createTime = createTime;
        }
    }

    public static final class WantedData {
        public final Long id;
        public final Long publisherId;
        public final String title;
        public final String budget;
        public final String category;
        public final String campus;
        public final String publisherName;
        public final LocalDateTime deadline;
        public final String intro;
        public final String description;
        public final List<String> tags;
        public final List<String> imageUrls;
        public final String coverStyle;
        public final String phone;
        public final String qq;
        public final String status;
        public final boolean deleted;

        public WantedData(Long id, Long publisherId, String title, String budget, String category, String campus, String publisherName, LocalDateTime deadline, String intro, String description, List<String> tags, List<String> imageUrls, String coverStyle, String phone, String qq, String status, boolean deleted) {
            this.id = id;
            this.publisherId = publisherId;
            this.title = title;
            this.budget = budget;
            this.category = category;
            this.campus = campus;
            this.publisherName = publisherName;
            this.deadline = deadline;
            this.intro = intro;
            this.description = description;
            this.tags = tags;
            this.imageUrls = imageUrls == null ? List.of() : List.copyOf(imageUrls);
            this.coverStyle = coverStyle;
            this.phone = phone;
            this.qq = qq;
            this.status = status;
            this.deleted = deleted;
        }
    }

    public static final class AnnouncementData {
        public final Long id;
        public final String title;
        public final String summary;
        public final String content;
        public final LocalDateTime publishedAt;
        public final String level;
        public final boolean top;
        public final boolean published;
        public final boolean deleted;

        public AnnouncementData(Long id, String title, String summary, String content, LocalDateTime publishedAt, String level, boolean top, boolean published, boolean deleted) {
            this.id = id;
            this.title = title;
            this.summary = summary;
            this.content = content;
            this.publishedAt = publishedAt;
            this.level = level;
            this.top = top;
            this.published = published;
            this.deleted = deleted;
        }
    }

    public static final class AppointmentData {
        public final Long id;
        public final Long goodsId;
        public final String goodsTitle;
        public final Long buyerId;
        public final String buyerName;
        public final Long sellerId;
        public final String sellerName;
        public final LocalDateTime intendedTime;
        public final String intendedLocation;
        public final String remark;
        public String status;
        public final boolean deleted;

        public AppointmentData(Long id, Long goodsId, String goodsTitle, Long buyerId, String buyerName, Long sellerId, String sellerName, LocalDateTime intendedTime, String intendedLocation, String remark, String status, boolean deleted) {
            this.id = id;
            this.goodsId = goodsId;
            this.goodsTitle = goodsTitle;
            this.buyerId = buyerId;
            this.buyerName = buyerName;
            this.sellerId = sellerId;
            this.sellerName = sellerName;
            this.intendedTime = intendedTime;
            this.intendedLocation = intendedLocation;
            this.remark = remark;
            this.status = status;
            this.deleted = deleted;
        }
    }

    public static final class NotificationData {
        public final Long id;
        public final String title;
        public final String content;
        public final String type;
        public boolean read;
        public final Long relatedId;
        public final LocalDateTime createTime;

        public NotificationData(Long id, String title, String content, String type, boolean read, Long relatedId, LocalDateTime createTime) {
            this.id = id;
            this.title = title;
            this.content = content;
            this.type = type;
            this.read = read;
            this.relatedId = relatedId;
            this.createTime = createTime;
        }
    }

    public static final class CategoryData {
        public final Long id;
        public final String name;
        public final int sortNum;
        public final boolean enabled;
        public final boolean deleted;

        public CategoryData(Long id, String name, int sortNum, boolean enabled, boolean deleted) {
            this.id = id;
            this.name = name;
            this.sortNum = sortNum;
            this.enabled = enabled;
            this.deleted = deleted;
        }
    }

    public static final class BannerData {
        public final Long id;
        public final String title;
        public final String imageUrl;
        public final String jumpType;
        public final String jumpTarget;
        public final int sortNum;
        public final boolean enabled;
        public final boolean deleted;

        public BannerData(Long id, String title, String imageUrl, String jumpType, String jumpTarget, int sortNum, boolean enabled, boolean deleted) {
            this.id = id;
            this.title = title;
            this.imageUrl = imageUrl;
            this.jumpType = jumpType;
            this.jumpTarget = jumpTarget;
            this.sortNum = sortNum;
            this.enabled = enabled;
            this.deleted = deleted;
        }
    }
}

