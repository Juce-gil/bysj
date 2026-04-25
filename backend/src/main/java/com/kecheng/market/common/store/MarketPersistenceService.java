package com.kecheng.market.common.store;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kecheng.market.admin.dto.AdminAnnouncementQuery;
import com.kecheng.market.admin.dto.AdminAnnouncementSaveRequest;
import com.kecheng.market.admin.dto.AdminGoodsQuery;
import com.kecheng.market.admin.dto.AdminUserQuery;
import com.kecheng.market.admin.vo.AdminAnnouncementDetailVo;
import com.kecheng.market.admin.vo.AdminAnnouncementListItemVo;
import com.kecheng.market.admin.vo.AdminGoodsDetailVo;
import com.kecheng.market.admin.vo.AdminGoodsListItemVo;
import com.kecheng.market.admin.vo.AdminUserListItemVo;
import com.kecheng.market.announcement.entity.AnnouncementEntity;
import com.kecheng.market.announcement.mapper.AnnouncementMapper;
import com.kecheng.market.announcement.vo.AnnouncementVo;
import com.kecheng.market.appointment.entity.AppointmentEntity;
import com.kecheng.market.appointment.mapper.AppointmentMapper;
import com.kecheng.market.appointment.vo.AppointmentVo;
import com.kecheng.market.banner.entity.BannerEntity;
import com.kecheng.market.banner.mapper.BannerMapper;
import com.kecheng.market.banner.vo.BannerVo;
import com.kecheng.market.category.entity.CategoryEntity;
import com.kecheng.market.category.mapper.CategoryMapper;
import com.kecheng.market.category.vo.CategoryVo;
import com.kecheng.market.comment.entity.CommentEntity;
import com.kecheng.market.comment.mapper.CommentMapper;
import com.kecheng.market.comment.vo.CommentVo;
import com.kecheng.market.common.PageResult;
import com.kecheng.market.common.exception.BusinessException;
import com.kecheng.market.common.exception.NotFoundException;
import com.kecheng.market.common.util.DateTimeUtil;
import com.kecheng.market.favorite.entity.FavoriteEntity;
import com.kecheng.market.favorite.mapper.FavoriteMapper;
import com.kecheng.market.goods.entity.GoodsEntity;
import com.kecheng.market.goods.mapper.GoodsMapper;
import com.kecheng.market.goods.vo.GoodsVo;
import com.kecheng.market.home.vo.HomeDataVo;
import com.kecheng.market.home.vo.HomeStatVo;
import com.kecheng.market.notification.entity.NotificationEntity;
import com.kecheng.market.notification.mapper.NotificationMapper;
import com.kecheng.market.notification.vo.NotificationVo;
import com.kecheng.market.user.entity.UserEntity;
import com.kecheng.market.user.mapper.UserMapper;
import com.kecheng.market.user.vo.UserProfileVo;
import com.kecheng.market.wanted.entity.WantedEntity;
import com.kecheng.market.wanted.mapper.WantedMapper;
import com.kecheng.market.wanted.vo.WantedVo;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@ConditionalOnProperty(name = "market.storage-mode", havingValue = "mysql")
public class MarketPersistenceService {

    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {
    };

    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;
    private final GoodsMapper goodsMapper;
    private final WantedMapper wantedMapper;
    private final CommentMapper commentMapper;
    private final FavoriteMapper favoriteMapper;
    private final AppointmentMapper appointmentMapper;
    private final NotificationMapper notificationMapper;
    private final AnnouncementMapper announcementMapper;
    private final BannerMapper bannerMapper;
    private final ObjectMapper objectMapper;
    private final String schoolName;

    public MarketPersistenceService(
            UserMapper userMapper,
            CategoryMapper categoryMapper,
            GoodsMapper goodsMapper,
            WantedMapper wantedMapper,
            CommentMapper commentMapper,
            FavoriteMapper favoriteMapper,
            AppointmentMapper appointmentMapper,
            NotificationMapper notificationMapper,
            AnnouncementMapper announcementMapper,
            BannerMapper bannerMapper,
            ObjectMapper objectMapper,
            @Value("${market.school-name:Kecheng Campus}") String schoolName) {
        this.userMapper = userMapper;
        this.categoryMapper = categoryMapper;
        this.goodsMapper = goodsMapper;
        this.wantedMapper = wantedMapper;
        this.commentMapper = commentMapper;
        this.favoriteMapper = favoriteMapper;
        this.appointmentMapper = appointmentMapper;
        this.notificationMapper = notificationMapper;
        this.announcementMapper = announcementMapper;
        this.bannerMapper = bannerMapper;
        this.objectMapper = objectMapper;
        this.schoolName = schoolName;
    }

    public UserEntity findUserByUsernameOrStudentNo(String account) {
        String normalized = requireText(account, "Account or password is incorrect");
        UserEntity entity = userMapper.selectOne(Wrappers.<UserEntity>lambdaQuery()
                .and(wrapper -> wrapper.eq(UserEntity::getUsername, normalized)
                        .or()
                        .eq(UserEntity::getStudentNo, normalized))
                .last("LIMIT 1"));
        if (entity == null) {
            throw new BusinessException(400, "Account or password is incorrect");
        }
        return entity;
    }

    public UserProfileVo getUserProfile(Long userId) {
        return toUserProfileVo(getUser(userId));
    }

    @Transactional
    public UserProfileVo updateUserProfile(Long userId, String name, String phone, String qq, String slogan) {
        UserEntity user = getUser(userId);
        String normalizedPhone = requireText(phone, "Phone cannot be blank");
        Long duplicateCount = userMapper.selectCount(Wrappers.<UserEntity>lambdaQuery()
                .eq(UserEntity::getPhone, normalizedPhone)
                .ne(UserEntity::getId, userId));
        if (duplicateCount != null && duplicateCount > 0) {
            throw new BusinessException(400, "Phone already exists");
        }
        user.setRealName(requireText(name, "Name cannot be blank"));
        user.setPhone(normalizedPhone);
        user.setQq(normalizeOptionalText(qq));
        user.setSlogan(normalizeOptionalText(slogan));
        userMapper.updateById(user);
        return toUserProfileVo(user);
    }

    @Transactional
    public UserProfileVo registerUser(String username, String password, String displayName) {
        String normalizedUsername = requireText(username, "Username cannot be blank");
        String normalizedPassword = requireText(password, "Password cannot be blank");
        UserEntity existing = userMapper.selectOne(Wrappers.<UserEntity>lambdaQuery()
                .eq(UserEntity::getUsername, normalizedUsername)
                .last("LIMIT 1"));
        if (existing != null) {
            throw new BusinessException(400, "Username already exists");
        }

        UserEntity entity = new UserEntity();
        entity.setUsername(normalizedUsername);
        entity.setPassword(normalizedPassword);
        entity.setRealName(StringUtils.hasText(displayName) ? displayName.trim() : normalizedUsername);
        entity.setStudentNo(generateUniqueStudentNo());
        entity.setPhone(generateUniquePhone());
        entity.setEmail("");
        entity.setGender("unknown");
        entity.setQq("");
        entity.setAvatar("/uploads/avatar/default.png");
        entity.setRole("user");
        entity.setStatus("normal");
        entity.setSchool(this.schoolName);
        entity.setSlogan("Hope every idle item finds a new owner.");
        userMapper.insert(entity);
        return toUserProfileVo(entity);
    }

    public List<CategoryVo> listCategories() {
        return categoryMapper.selectList(Wrappers.<CategoryEntity>lambdaQuery()
                        .eq(CategoryEntity::getStatus, 1)
                        .orderByAsc(CategoryEntity::getSortNum, CategoryEntity::getId))
                .stream()
                .map(item -> new CategoryVo(item.getId(), item.getName(), item.getSortNum()))
                .toList();
    }

    public List<GoodsVo> listGoods(Long currentUserId) {
        List<GoodsEntity> goods = goodsMapper.selectList(Wrappers.<GoodsEntity>lambdaQuery()
                .orderByDesc(GoodsEntity::getFavoriteCount)
                .orderByAsc(GoodsEntity::getId));
        return toGoodsVoList(goods, currentUserId);
    }

    public List<GoodsVo> listMyGoods(Long userId) {
        List<GoodsEntity> goods = goodsMapper.selectList(Wrappers.<GoodsEntity>lambdaQuery()
                .eq(GoodsEntity::getUserId, userId)
                .orderByDesc(GoodsEntity::getPublishedAt, GoodsEntity::getId));
        return toGoodsVoList(goods, userId);
    }

    public GoodsVo getGoodsDetail(Long goodsId, Long currentUserId) {
        return toGoodsVo(getGoods(goodsId), currentUserId, null, null, loadFavoriteGoodsIds(currentUserId));
    }

    @Transactional
    public boolean toggleFavorite(Long userId, Long goodsId) {
        GoodsEntity goods = getGoods(goodsId);
        FavoriteEntity favorite = favoriteMapper.selectAnyByUserIdAndGoodsId(userId, goodsId);
        boolean nowFavorited;
        if (favorite != null && favorite.getDeleted() != null && favorite.getDeleted() == 0) {
            favoriteMapper.updateDeletedFlag(favorite.getId(), 1);
            goods.setFavoriteCount(Math.max(0L, defaultLong(goods.getFavoriteCount()) - 1L));
            nowFavorited = false;
        } else {
            if (favorite != null) {
                favoriteMapper.updateDeletedFlag(favorite.getId(), 0);
            } else {
                FavoriteEntity entity = new FavoriteEntity();
                entity.setUserId(userId);
                entity.setGoodsId(goodsId);
                favoriteMapper.insert(entity);
            }
            goods.setFavoriteCount(defaultLong(goods.getFavoriteCount()) + 1L);
            nowFavorited = true;
            addNotification(goods.getUserId(), "Goods favorited", "Your goods [" + goods.getTitle() + "] received a new favorite.", "favorite", goodsId);
        }
        goodsMapper.updateById(goods);
        return nowFavorited;
    }

    public List<GoodsVo> listMyFavorites(Long userId) {
        List<FavoriteEntity> favorites = favoriteMapper.selectList(Wrappers.<FavoriteEntity>lambdaQuery()
                .eq(FavoriteEntity::getUserId, userId)
                .orderByDesc(FavoriteEntity::getId));
        if (favorites.isEmpty()) {
            return List.of();
        }
        Map<Long, GoodsEntity> goodsMap = loadGoodsMap(favorites.stream().map(FavoriteEntity::getGoodsId).toList());
        Set<Long> favoriteIds = favorites.stream()
                .map(FavoriteEntity::getGoodsId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        List<GoodsVo> result = new ArrayList<>();
        for (FavoriteEntity favorite : favorites) {
            GoodsEntity goods = goodsMap.get(favorite.getGoodsId());
            if (goods != null) {
                result.add(toGoodsVo(goods, userId, null, null, favoriteIds));
            }
        }
        return result;
    }

    @Transactional
    public GoodsVo createGoods(Long userId, String title, BigDecimal price, BigDecimal originalPrice, String category, String campus,
                               String condition, String intro, String description, List<String> tags, List<String> imageUrls) {
        UserEntity seller = getUser(userId);
        CategoryEntity categoryEntity = requireEnabledCategory(category);
        GoodsEntity entity = new GoodsEntity();
        entity.setUserId(userId);
        entity.setTitle(requireText(title, "Title cannot be blank"));
        entity.setPrice(requirePositivePrice(price, "Price must be greater than 0"));
        entity.setOriginalPrice(normalizeOriginalPrice(originalPrice));
        entity.setCategoryId(categoryEntity.getId());
        entity.setCampus(requireText(campus, "Campus cannot be blank"));
        entity.setConditionLevel(requireText(condition, "Condition cannot be blank"));
        entity.setIntro(requireText(intro, "Intro cannot be blank"));
        entity.setDescription(requireText(description, "Description cannot be blank"));
        entity.setTagsJson(writeStringList(normalizeTags(tags)));
        entity.setImageUrlsJson(writeStringList(normalizeImageUrls(imageUrls)));
        entity.setCoverStyle(generateCoverStyle(categoryEntity.getName(), entity.getTitle()));
        entity.setNegotiable(Boolean.FALSE);
        entity.setFavoriteCount(0L);
        entity.setStatus("on_sale");
        entity.setPublishedAt(LocalDateTime.now());
        goodsMapper.insert(entity);
        addNotification(userId, "Goods published", "Your goods [" + entity.getTitle() + "] is now visible in the marketplace.", "goods_publish", entity.getId());
        return toGoodsVo(entity, userId, seller, categoryEntity, Set.of());
    }

    @Transactional
    public GoodsVo updateGoods(Long userId, Long goodsId, String title, BigDecimal price, BigDecimal originalPrice, String category,
                               String campus, String condition, String intro, String description, List<String> tags, List<String> imageUrls) {
        GoodsEntity goods = getOwnedGoods(userId, goodsId);
        if ("reserved".equals(goods.getStatus()) || "sold".equals(goods.getStatus())) {
            throw new BusinessException(400, "Current goods cannot be edited");
        }
        CategoryEntity categoryEntity = requireEnabledCategory(category);
        goods.setTitle(requireText(title, "Title cannot be blank"));
        goods.setPrice(requirePositivePrice(price, "Price must be greater than 0"));
        goods.setOriginalPrice(normalizeOriginalPrice(originalPrice));
        goods.setCategoryId(categoryEntity.getId());
        goods.setCampus(requireText(campus, "Campus cannot be blank"));
        goods.setConditionLevel(requireText(condition, "Condition cannot be blank"));
        goods.setIntro(requireText(intro, "Intro cannot be blank"));
        goods.setDescription(requireText(description, "Description cannot be blank"));
        goods.setTagsJson(writeStringList(normalizeTags(tags)));
        goods.setImageUrlsJson(writeStringList(normalizeImageUrls(imageUrls)));
        goods.setCoverStyle(generateCoverStyle(categoryEntity.getName(), goods.getTitle()));
        goodsMapper.updateById(goods);
        addNotification(userId, "Goods updated", "Your goods information has been updated.", "goods_update", goodsId);
        return toGoodsVo(goods, userId, null, categoryEntity, loadFavoriteGoodsIds(userId));
    }

    @Transactional
    public GoodsVo offShelfGoods(Long userId, Long goodsId) {
        return offShelfGoodsInternal(getOwnedGoods(userId, goodsId), true);
    }

    @Transactional
    public GoodsVo relistGoods(Long userId, Long goodsId) {
        return relistGoodsInternal(getOwnedGoods(userId, goodsId), true);
    }

    @Transactional
    public void deleteGoods(Long userId, Long goodsId) {
        deleteGoodsInternal(getOwnedGoods(userId, goodsId), true);
    }

    public List<CommentVo> listComments(Long goodsId) {
        List<CommentEntity> comments = commentMapper.selectList(Wrappers.<CommentEntity>lambdaQuery()
                .eq(CommentEntity::getGoodsId, goodsId)
                .orderByAsc(CommentEntity::getCreateTime, CommentEntity::getId));
        Map<Long, UserEntity> users = loadUsers(comments.stream().map(CommentEntity::getUserId).toList());
        return comments.stream()
                .map(item -> new CommentVo(
                        item.getId(),
                        item.getGoodsId(),
                        item.getUserId(),
                        displayName(users.get(item.getUserId())),
                        item.getContent(),
                        DateTimeUtil.formatDateTime(item.getCreateTime())))
                .toList();
    }

    @Transactional
    public CommentVo addComment(Long goodsId, Long userId, String content) {
        GoodsEntity goods = getGoods(goodsId);
        UserEntity user = getUser(userId);
        CommentEntity entity = new CommentEntity();
        entity.setGoodsId(goodsId);
        entity.setUserId(userId);
        entity.setContent(requireText(content, "Comment content cannot be blank"));
        commentMapper.insert(entity);
        addNotification(goods.getUserId(), "New comment", user.getRealName() + " commented on your goods [" + goods.getTitle() + "].", "comment", goodsId);
        return new CommentVo(entity.getId(), goodsId, userId, user.getRealName(), entity.getContent(), DateTimeUtil.formatDateTime(entity.getCreateTime()));
    }

    public List<WantedVo> listWanted(Long currentUserId) {
        List<WantedEntity> wantedList = wantedMapper.selectList(Wrappers.<WantedEntity>lambdaQuery().orderByAsc(WantedEntity::getId));
        return toWantedVoList(wantedList, currentUserId != null);
    }

    public List<WantedVo> listMyWanted(Long userId) {
        List<WantedEntity> wantedList = wantedMapper.selectList(Wrappers.<WantedEntity>lambdaQuery()
                .eq(WantedEntity::getUserId, userId)
                .orderByDesc(WantedEntity::getDeadline, WantedEntity::getId));
        return toWantedVoList(wantedList, true);
    }

    public WantedVo getWantedDetail(Long wantedId, boolean loggedIn) {
        return toWantedVo(getWanted(wantedId), loggedIn, null, null);
    }

    @Transactional
    public WantedVo createWanted(Long userId, String title, String budget, String category, String campus, String deadline,
                                 String intro, String description, List<String> tags, List<String> imageUrls) {
        CategoryEntity categoryEntity = requireEnabledCategory(category);
        WantedEntity entity = new WantedEntity();
        entity.setUserId(userId);
        entity.setTitle(requireText(title, "Title cannot be blank"));
        entity.setBudget(requireText(budget, "Budget cannot be blank"));
        entity.setCategoryId(categoryEntity.getId());
        entity.setCampus(requireText(campus, "Campus cannot be blank"));
        entity.setDeadline(parseWantedDeadline(deadline));
        entity.setIntro(requireText(intro, "Intro cannot be blank"));
        entity.setDescription(requireText(description, "Description cannot be blank"));
        entity.setTagsJson(writeStringList(normalizeTags(tags)));
        entity.setImageUrlsJson(writeStringList(normalizeImageUrls(imageUrls)));
        entity.setCoverStyle(generateCoverStyle(categoryEntity.getName(), entity.getTitle()));
        entity.setStatus("buying");
        wantedMapper.insert(entity);
        addNotification(userId, "Wanted published", "Your wanted post has been published successfully.", "wanted_publish", entity.getId());
        return toWantedVo(entity, true, getUser(userId), categoryEntity);
    }

    @Transactional
    public WantedVo updateWanted(Long userId, Long wantedId, String title, String budget, String category, String campus, String deadline,
                                 String intro, String description, List<String> tags, List<String> imageUrls) {
        WantedEntity wanted = getOwnedWanted(userId, wantedId);
        if ("finished".equals(wanted.getStatus())) {
            throw new BusinessException(400, "Finished wanted post cannot be edited");
        }
        CategoryEntity categoryEntity = requireEnabledCategory(category);
        wanted.setTitle(requireText(title, "Title cannot be blank"));
        wanted.setBudget(requireText(budget, "Budget cannot be blank"));
        wanted.setCategoryId(categoryEntity.getId());
        wanted.setCampus(requireText(campus, "Campus cannot be blank"));
        wanted.setDeadline(parseWantedDeadline(deadline));
        wanted.setIntro(requireText(intro, "Intro cannot be blank"));
        wanted.setDescription(requireText(description, "Description cannot be blank"));
        wanted.setTagsJson(writeStringList(normalizeTags(tags)));
        wanted.setImageUrlsJson(writeStringList(normalizeImageUrls(imageUrls)));
        wanted.setCoverStyle(generateCoverStyle(categoryEntity.getName(), wanted.getTitle()));
        wantedMapper.updateById(wanted);
        addNotification(userId, "Wanted updated", "Your wanted post has been updated.", "wanted_update", wantedId);
        return toWantedVo(wanted, true, getUser(userId), categoryEntity);
    }

    @Transactional
    public WantedVo closeWanted(Long userId, Long wantedId) {
        WantedEntity wanted = getOwnedWanted(userId, wantedId);
        if ("closed".equals(wanted.getStatus())) {
            return toWantedVo(wanted, true, null, null);
        }
        if ("finished".equals(wanted.getStatus())) {
            throw new BusinessException(400, "Finished wanted post cannot be closed");
        }
        wanted.setStatus("closed");
        wantedMapper.updateById(wanted);
        addNotification(userId, "Wanted closed", "Your wanted post has been closed.", "wanted_close", wantedId);
        return toWantedVo(wanted, true, null, null);
    }

    @Transactional
    public WantedVo reopenWanted(Long userId, Long wantedId) {
        WantedEntity wanted = getOwnedWanted(userId, wantedId);
        if ("buying".equals(wanted.getStatus())) {
            return toWantedVo(wanted, true, null, null);
        }
        if (!"closed".equals(wanted.getStatus())) {
            throw new BusinessException(400, "Current wanted post cannot be reopened");
        }
        wanted.setStatus("buying");
        wantedMapper.updateById(wanted);
        addNotification(userId, "Wanted reopened", "Your wanted post has been reopened.", "wanted_reopen", wantedId);
        return toWantedVo(wanted, true, null, null);
    }

    @Transactional
    public void deleteWanted(Long userId, Long wantedId) {
        WantedEntity wanted = getOwnedWanted(userId, wantedId);
        wantedMapper.deleteById(wanted.getId());
        addNotification(userId, "Wanted deleted", "Your wanted post has been removed.", "wanted_delete", wantedId);
    }

    public List<AnnouncementVo> listAnnouncements() {
        return announcementMapper.selectList(Wrappers.<AnnouncementEntity>lambdaQuery()
                        .eq(AnnouncementEntity::getPublished, Boolean.TRUE)
                        .orderByDesc(AnnouncementEntity::getTop)
                        .orderByAsc(AnnouncementEntity::getId))
                .stream()
                .map(this::toAnnouncementVo)
                .toList();
    }

    public AnnouncementVo getAnnouncementDetail(Long announcementId) {
        AnnouncementEntity entity = announcementMapper.selectOne(Wrappers.<AnnouncementEntity>lambdaQuery()
                .eq(AnnouncementEntity::getId, announcementId)
                .eq(AnnouncementEntity::getPublished, Boolean.TRUE)
                .last("LIMIT 1"));
        if (entity == null) {
            throw new NotFoundException("Announcement not found");
        }
        return toAnnouncementVo(entity);
    }

    public List<BannerVo> listBanners() {
        return bannerMapper.selectList(Wrappers.<BannerEntity>lambdaQuery()
                        .eq(BannerEntity::getStatus, 1)
                        .orderByAsc(BannerEntity::getSortNum, BannerEntity::getId))
                .stream()
                .map(item -> new BannerVo(item.getId(), item.getTitle(), item.getImageUrl(), item.getJumpType(), item.getJumpTarget(), item.getSortNum()))
                .toList();
    }

    public HomeDataVo getHomeData(Long currentUserId) {
        long onSaleCount = safeCount(goodsMapper.selectCount(Wrappers.<GoodsEntity>lambdaQuery().eq(GoodsEntity::getStatus, "on_sale")));
        long buyingCount = safeCount(wantedMapper.selectCount(Wrappers.<WantedEntity>lambdaQuery().eq(WantedEntity::getStatus, "buying")));
        long announcementCount = safeCount(announcementMapper.selectCount(Wrappers.<AnnouncementEntity>lambdaQuery().eq(AnnouncementEntity::getPublished, Boolean.TRUE)));
        long categoryCount = safeCount(categoryMapper.selectCount(Wrappers.<CategoryEntity>lambdaQuery().eq(CategoryEntity::getStatus, 1)));
        return new HomeDataVo(
                List.of(
                        new HomeStatVo("在售闲置", String.format("%02d", onSaleCount)),
                        new HomeStatVo("求购意向", String.format("%02d", buyingCount)),
                        new HomeStatVo("站内公告", String.format("%02d", announcementCount)),
                        new HomeStatVo("平台分类", String.format("%02d", categoryCount))
                ),
                limit(listGoods(currentUserId), 3),
                limit(listWanted(currentUserId), 2),
                limit(listAnnouncements(), 3),
                listBanners());
    }

    @Transactional
    public AppointmentVo createAppointment(Long buyerId, Long goodsId, LocalDateTime intendedTime, String intendedLocation, String remark) {
        GoodsEntity goods = getGoods(goodsId);
        if (!"on_sale".equals(goods.getStatus())) {
            throw new BusinessException(400, "Current goods cannot be reserved");
        }
        if (Objects.equals(goods.getUserId(), buyerId)) {
            throw new BusinessException(400, "Cannot reserve your own goods");
        }
        UserEntity buyer = getUser(buyerId);
        UserEntity seller = getUser(goods.getUserId());
        AppointmentEntity entity = new AppointmentEntity();
        entity.setGoodsId(goodsId);
        entity.setBuyerId(buyerId);
        entity.setSellerId(goods.getUserId());
        entity.setIntendedTime(intendedTime);
        entity.setIntendedLocation(requireText(intendedLocation, "Intended location cannot be blank"));
        entity.setRemark(normalizeOptionalText(remark));
        entity.setApplyTime(LocalDateTime.now());
        entity.setStatus("pending");
        appointmentMapper.insert(entity);

        goods.setStatus("reserved");
        goodsMapper.updateById(goods);

        addNotification(seller.getId(), "New appointment", buyer.getRealName() + " reserved your goods [" + goods.getTitle() + "].", "appointment_apply", entity.getId());
        addNotification(buyer.getId(), "Appointment submitted", "Your appointment for [" + goods.getTitle() + "] has been submitted.", "appointment_apply", entity.getId());
        return toAppointmentVo(entity, goods, buyer, seller);
    }

    public List<AppointmentVo> listMyAppointments(Long userId) {
        List<AppointmentEntity> appointments = appointmentMapper.selectList(Wrappers.<AppointmentEntity>lambdaQuery()
                .and(wrapper -> wrapper.eq(AppointmentEntity::getBuyerId, userId)
                        .or()
                        .eq(AppointmentEntity::getSellerId, userId))
                .orderByDesc(AppointmentEntity::getId));
        return toAppointmentVoList(appointments);
    }

    @Transactional
    public AppointmentVo cancelAppointment(Long userId, Long appointmentId) {
        AppointmentEntity appointment = getAppointment(appointmentId);
        if (!Objects.equals(appointment.getBuyerId(), userId)) {
            throw new BusinessException(403, "Only the buyer can cancel the appointment");
        }
        if (!"pending".equals(appointment.getStatus()) && !"agreed".equals(appointment.getStatus())) {
            throw new BusinessException(400, "Current appointment status does not allow cancellation");
        }
        appointment.setStatus("cancelled");
        appointment.setHandleTime(LocalDateTime.now());
        appointmentMapper.updateById(appointment);

        GoodsEntity goods = getGoods(appointment.getGoodsId());
        goods.setStatus("on_sale");
        goodsMapper.updateById(goods);

        addNotification(appointment.getSellerId(), "Appointment cancelled", displayName(getUser(appointment.getBuyerId())) + " cancelled the appointment for [" + goods.getTitle() + "].", "appointment_cancel", appointmentId);
        return toAppointmentVo(appointment, goods, null, null);
    }

    public List<NotificationVo> listNotifications(Long userId) {
        return notificationMapper.selectList(Wrappers.<NotificationEntity>lambdaQuery()
                        .eq(NotificationEntity::getUserId, userId)
                        .orderByDesc(NotificationEntity::getId))
                .stream()
                .map(this::toNotificationVo)
                .toList();
    }

    public long unreadNotificationCount(Long userId) {
        return safeCount(notificationMapper.selectCount(Wrappers.<NotificationEntity>lambdaQuery()
                .eq(NotificationEntity::getUserId, userId)
                .eq(NotificationEntity::getIsRead, Boolean.FALSE)));
    }

    @Transactional
    public void markNotificationRead(Long userId, Long notificationId) {
        NotificationEntity entity = notificationMapper.selectOne(Wrappers.<NotificationEntity>lambdaQuery()
                .eq(NotificationEntity::getId, notificationId)
                .eq(NotificationEntity::getUserId, userId)
                .last("LIMIT 1"));
        if (entity == null) {
            throw new NotFoundException("Notification not found");
        }
        entity.setIsRead(Boolean.TRUE);
        notificationMapper.updateById(entity);
    }

    @Transactional
    public void markAllNotificationsRead(Long userId) {
        List<NotificationEntity> notifications = notificationMapper.selectList(Wrappers.<NotificationEntity>lambdaQuery()
                .eq(NotificationEntity::getUserId, userId)
                .eq(NotificationEntity::getIsRead, Boolean.FALSE));
        for (NotificationEntity notification : notifications) {
            notification.setIsRead(Boolean.TRUE);
            notificationMapper.updateById(notification);
        }
    }

    public PageResult<AdminUserListItemVo> pageAdminUsers(AdminUserQuery query) {
        List<AdminUserListItemVo> all = userMapper.selectList(Wrappers.<UserEntity>lambdaQuery().orderByAsc(UserEntity::getId))
                .stream()
                .filter(item -> matchesKeyword(query.getKeyword(), item.getUsername(), item.getRealName(), item.getStudentNo(), item.getPhone()))
                .filter(item -> !StringUtils.hasText(query.getRole()) || Objects.equals(item.getRole(), query.getRole()))
                .filter(item -> query.getDisabled() == null || Objects.equals(isDisabled(item), query.getDisabled()))
                .sorted(Comparator.comparing((UserEntity item) -> "admin".equals(item.getRole()) ? 0 : 1).thenComparing(UserEntity::getId))
                .map(this::toAdminUserListItem)
                .toList();
        return toPageResult(all, query.getPageNum(), query.getPageSize());
    }

    @Transactional
    public AdminUserListItemVo updateAdminUserStatus(Long currentAdminId, Long targetUserId, boolean disabled) {
        if (disabled && Objects.equals(currentAdminId, targetUserId)) {
            throw new BusinessException(400, "Admin cannot disable self");
        }
        UserEntity user = getUser(targetUserId);
        user.setStatus(disabled ? "disabled" : "normal");
        userMapper.updateById(user);
        return toAdminUserListItem(user);
    }

    public PageResult<AdminGoodsListItemVo> pageAdminGoods(AdminGoodsQuery query) {
        List<GoodsEntity> goods = goodsMapper.selectList(Wrappers.<GoodsEntity>lambdaQuery()
                .orderByDesc(GoodsEntity::getPublishedAt, GoodsEntity::getId));
        Map<Long, UserEntity> users = loadUsers(goods.stream().map(GoodsEntity::getUserId).toList());
        Map<Long, CategoryEntity> categories = loadCategories(goods.stream().map(GoodsEntity::getCategoryId).toList());
        List<AdminGoodsListItemVo> all = goods.stream()
                .filter(item -> matchesKeyword(query.getKeyword(), item.getTitle(), displayName(users.get(item.getUserId())), categoryName(categories.get(item.getCategoryId()))))
                .filter(item -> !StringUtils.hasText(query.getStatus()) || Objects.equals(item.getStatus(), query.getStatus()))
                .filter(item -> !StringUtils.hasText(query.getCategory()) || Objects.equals(categoryName(categories.get(item.getCategoryId())), query.getCategory()))
                .map(item -> toAdminGoodsListItem(item, users.get(item.getUserId()), categories.get(item.getCategoryId())))
                .toList();
        return toPageResult(all, query.getPageNum(), query.getPageSize());
    }

    public AdminGoodsDetailVo getAdminGoodsDetail(Long goodsId) {
        return toAdminGoodsDetailVo(getGoods(goodsId), null, null);
    }

    @Transactional
    public AdminGoodsDetailVo offShelfAdminGoods(Long goodsId) {
        GoodsEntity goods = getGoods(goodsId);
        offShelfGoodsInternal(goods, false);
        return toAdminGoodsDetailVo(goods, null, null);
    }

    @Transactional
    public AdminGoodsDetailVo relistAdminGoods(Long goodsId) {
        GoodsEntity goods = getGoods(goodsId);
        relistGoodsInternal(goods, false);
        return toAdminGoodsDetailVo(goods, null, null);
    }

    @Transactional
    public void deleteAdminGoods(Long goodsId) {
        deleteGoodsInternal(getGoods(goodsId), false);
    }

    public PageResult<AdminAnnouncementListItemVo> pageAdminAnnouncements(AdminAnnouncementQuery query) {
        List<AdminAnnouncementListItemVo> all = announcementMapper.selectList(Wrappers.<AnnouncementEntity>lambdaQuery())
                .stream()
                .filter(item -> matchesKeyword(query.getKeyword(), item.getTitle(), item.getSummary(), item.getContent()))
                .filter(item -> query.getPublished() == null || Objects.equals(Boolean.TRUE.equals(item.getPublished()), query.getPublished()))
                .sorted(Comparator.comparing((AnnouncementEntity item) -> Boolean.TRUE.equals(item.getTop()))
                        .reversed()
                        .thenComparing(AnnouncementEntity::getPublishedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(AnnouncementEntity::getId, Comparator.reverseOrder()))
                .map(this::toAdminAnnouncementListItem)
                .toList();
        return toPageResult(all, query.getPageNum(), query.getPageSize());
    }

    public AdminAnnouncementDetailVo getAdminAnnouncementDetail(Long announcementId) {
        return toAdminAnnouncementDetailVo(getAnnouncement(announcementId));
    }

    @Transactional
    public AdminAnnouncementDetailVo createAdminAnnouncement(AdminAnnouncementSaveRequest request) {
        AnnouncementEntity entity = new AnnouncementEntity();
        entity.setTitle(requireText(request.title(), "Title cannot be blank"));
        entity.setSummary(normalizeOptionalText(request.summary()));
        entity.setContent(requireText(request.content(), "Content cannot be blank"));
        entity.setLevel(normalizeAnnouncementLevel(request.level()));
        entity.setTop(Boolean.TRUE.equals(request.top()));
        entity.setPublished(Boolean.TRUE.equals(request.published()));
        entity.setPublishedAt(Boolean.TRUE.equals(request.published()) ? LocalDateTime.now() : null);
        announcementMapper.insert(entity);
        return toAdminAnnouncementDetailVo(entity);
    }

    @Transactional
    public AdminAnnouncementDetailVo updateAdminAnnouncement(Long announcementId, AdminAnnouncementSaveRequest request) {
        AnnouncementEntity entity = getAnnouncement(announcementId);
        boolean nextPublished = Boolean.TRUE.equals(request.published());
        LocalDateTime publishedAt = entity.getPublishedAt();
        if (nextPublished && publishedAt == null) {
            publishedAt = LocalDateTime.now();
        }
        entity.setTitle(requireText(request.title(), "Title cannot be blank"));
        entity.setSummary(normalizeOptionalText(request.summary()));
        entity.setContent(requireText(request.content(), "Content cannot be blank"));
        entity.setLevel(normalizeAnnouncementLevel(request.level()));
        entity.setTop(Boolean.TRUE.equals(request.top()));
        entity.setPublished(nextPublished);
        entity.setPublishedAt(publishedAt);
        announcementMapper.updateById(entity);
        return toAdminAnnouncementDetailVo(entity);
    }

    @Transactional
    public AdminAnnouncementDetailVo publishAdminAnnouncement(Long announcementId) {
        AnnouncementEntity entity = getAnnouncement(announcementId);
        entity.setPublished(Boolean.TRUE);
        entity.setPublishedAt(LocalDateTime.now());
        announcementMapper.updateById(entity);
        return toAdminAnnouncementDetailVo(entity);
    }

    @Transactional
    public AdminAnnouncementDetailVo offlineAdminAnnouncement(Long announcementId) {
        AnnouncementEntity entity = getAnnouncement(announcementId);
        entity.setPublished(Boolean.FALSE);
        announcementMapper.updateById(entity);
        return toAdminAnnouncementDetailVo(entity);
    }

    @Transactional
    public void deleteAdminAnnouncement(Long announcementId) {
        announcementMapper.deleteById(getAnnouncement(announcementId).getId());
    }

    private GoodsVo offShelfGoodsInternal(GoodsEntity goods, boolean selfAction) {
        if ("reserved".equals(goods.getStatus()) || "sold".equals(goods.getStatus())) {
            throw new BusinessException(400, "Current goods cannot be off shelf");
        }
        if ("off_shelf".equals(goods.getStatus())) {
            return toGoodsVo(goods, goods.getUserId(), null, null, loadFavoriteGoodsIds(goods.getUserId()));
        }
        goods.setStatus("off_shelf");
        goodsMapper.updateById(goods);
        if (selfAction) {
            addNotification(goods.getUserId(), "Goods off shelf", "Your goods has been taken off shelf.", "goods_off_shelf", goods.getId());
        } else {
            addNotification(goods.getUserId(), "Goods off shelf", "Your goods [" + goods.getTitle() + "] was taken off shelf by the administrator.", "admin_goods_off_shelf", goods.getId());
        }
        return toGoodsVo(goods, goods.getUserId(), null, null, loadFavoriteGoodsIds(goods.getUserId()));
    }

    private GoodsVo relistGoodsInternal(GoodsEntity goods, boolean selfAction) {
        if ("on_sale".equals(goods.getStatus())) {
            return toGoodsVo(goods, goods.getUserId(), null, null, loadFavoriteGoodsIds(goods.getUserId()));
        }
        if (!"off_shelf".equals(goods.getStatus())) {
            throw new BusinessException(400, "Current goods cannot be relisted");
        }
        goods.setStatus("on_sale");
        goodsMapper.updateById(goods);
        if (selfAction) {
            addNotification(goods.getUserId(), "Goods relisted", "Your goods has been relisted.", "goods_relist", goods.getId());
        } else {
            addNotification(goods.getUserId(), "Goods relisted", "Your goods [" + goods.getTitle() + "] has been relisted by the administrator.", "admin_goods_relist", goods.getId());
        }
        return toGoodsVo(goods, goods.getUserId(), null, null, loadFavoriteGoodsIds(goods.getUserId()));
    }

    private void deleteGoodsInternal(GoodsEntity goods, boolean selfAction) {
        if ("reserved".equals(goods.getStatus())) {
            throw new BusinessException(400, "Reserved goods cannot be deleted");
        }
        goodsMapper.deleteById(goods.getId());
        if (selfAction) {
            addNotification(goods.getUserId(), "Goods deleted", "Your goods has been removed from the marketplace.", "goods_delete", goods.getId());
        } else {
            addNotification(goods.getUserId(), "Goods deleted", "Your goods [" + goods.getTitle() + "] was removed by the administrator.", "admin_goods_delete", goods.getId());
        }
    }

    private UserEntity getUser(Long userId) {
        UserEntity entity = userMapper.selectById(userId);
        if (entity == null) {
            throw new NotFoundException("User not found");
        }
        return entity;
    }

    private GoodsEntity getGoods(Long goodsId) {
        GoodsEntity entity = goodsMapper.selectById(goodsId);
        if (entity == null) {
            throw new NotFoundException("Goods not found");
        }
        return entity;
    }

    private GoodsEntity getOwnedGoods(Long userId, Long goodsId) {
        GoodsEntity entity = getGoods(goodsId);
        if (!Objects.equals(entity.getUserId(), userId)) {
            throw new BusinessException(403, "No permission to operate this goods");
        }
        return entity;
    }

    private WantedEntity getWanted(Long wantedId) {
        WantedEntity entity = wantedMapper.selectById(wantedId);
        if (entity == null) {
            throw new NotFoundException("Wanted post not found");
        }
        return entity;
    }

    private WantedEntity getOwnedWanted(Long userId, Long wantedId) {
        WantedEntity entity = getWanted(wantedId);
        if (!Objects.equals(entity.getUserId(), userId)) {
            throw new BusinessException(403, "No permission to operate this wanted post");
        }
        return entity;
    }

    private AppointmentEntity getAppointment(Long appointmentId) {
        AppointmentEntity entity = appointmentMapper.selectById(appointmentId);
        if (entity == null) {
            throw new NotFoundException("Appointment not found");
        }
        return entity;
    }

    private AnnouncementEntity getAnnouncement(Long announcementId) {
        AnnouncementEntity entity = announcementMapper.selectById(announcementId);
        if (entity == null) {
            throw new NotFoundException("Announcement not found");
        }
        return entity;
    }

    private CategoryEntity requireEnabledCategory(String category) {
        String normalized = requireText(category, "Category cannot be blank");
        CategoryEntity entity = categoryMapper.selectOne(Wrappers.<CategoryEntity>lambdaQuery()
                .eq(CategoryEntity::getName, normalized)
                .eq(CategoryEntity::getStatus, 1)
                .last("LIMIT 1"));
        if (entity == null) {
            throw new BusinessException(400, "Category does not exist");
        }
        return entity;
    }

    private String requireText(String value, String message) {
        String normalized = normalizeOptionalText(value);
        if (!StringUtils.hasText(normalized)) {
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

    private BigDecimal requirePositivePrice(BigDecimal value, String message) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(400, message);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal normalizeOriginalPrice(BigDecimal value) {
        if (value == null) {
            return null;
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(400, "Original price must be greater than or equal to 0");
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private LocalDateTime parseWantedDeadline(String deadline) {
        String normalized = requireText(deadline, "Deadline cannot be blank");
        try {
            return LocalDate.parse(normalized).atStartOfDay();
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
            String value = normalizeOptionalText(tag);
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
            String value = normalizeOptionalText(imageUrl);
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
        int index = Math.abs(Objects.hash(normalizeOptionalText(category), normalizeOptionalText(title))) % palettes.size();
        return palettes.get(index);
    }

    private UserProfileVo toUserProfileVo(UserEntity entity) {
        return new UserProfileVo(entity.getId(), entity.getRealName(), entity.getRole(), entity.getSchool(), entity.getSlogan(), entity.getPhone(), entity.getQq());
    }

    private AdminUserListItemVo toAdminUserListItem(UserEntity entity) {
        return new AdminUserListItemVo(
                entity.getId(),
                entity.getUsername(),
                entity.getRealName(),
                entity.getRole(),
                entity.getStudentNo(),
                entity.getPhone(),
                entity.getSchool(),
                isDisabled(entity),
                DateTimeUtil.formatDateTime(entity.getCreateTime()));
    }

    private List<GoodsVo> toGoodsVoList(List<GoodsEntity> goodsList, Long currentUserId) {
        if (goodsList.isEmpty()) {
            return List.of();
        }
        Map<Long, UserEntity> users = loadUsers(goodsList.stream().map(GoodsEntity::getUserId).toList());
        Map<Long, CategoryEntity> categories = loadCategories(goodsList.stream().map(GoodsEntity::getCategoryId).toList());
        Set<Long> favoriteGoodsIds = loadFavoriteGoodsIds(currentUserId);
        return goodsList.stream()
                .map(item -> toGoodsVo(item, currentUserId, users.get(item.getUserId()), categories.get(item.getCategoryId()), favoriteGoodsIds))
                .toList();
    }

    private GoodsVo toGoodsVo(GoodsEntity entity, Long currentUserId, UserEntity seller, CategoryEntity category, Set<Long> favoriteGoodsIds) {
        UserEntity resolvedSeller = seller != null ? seller : loadUsers(List.of(entity.getUserId())).get(entity.getUserId());
        CategoryEntity resolvedCategory = category != null ? category : loadCategories(List.of(entity.getCategoryId())).get(entity.getCategoryId());
        List<String> imageUrls = readStringList(entity.getImageUrlsJson());
        String coverImageUrl = imageUrls.isEmpty() ? "" : imageUrls.get(0);
        boolean favorited = currentUserId != null && favoriteGoodsIds.contains(entity.getId());
        return new GoodsVo(
                entity.getId(),
                entity.getUserId(),
                entity.getTitle(),
                entity.getPrice(),
                entity.getOriginalPrice(),
                categoryName(resolvedCategory),
                entity.getCampus(),
                entity.getConditionLevel(),
                displayName(resolvedSeller),
                DateTimeUtil.formatDateTime(resolvePublishedAt(entity.getPublishedAt(), entity.getCreateTime())),
                entity.getIntro(),
                entity.getDescription(),
                readStringList(entity.getTagsJson()),
                imageUrls,
                coverImageUrl,
                StringUtils.hasText(entity.getCoverStyle()) ? entity.getCoverStyle() : generateCoverStyle(categoryName(resolvedCategory), entity.getTitle()),
                defaultLong(entity.getFavoriteCount()),
                favorited,
                entity.getStatus());
    }

    private AdminGoodsListItemVo toAdminGoodsListItem(GoodsEntity entity, UserEntity seller, CategoryEntity category) {
        return new AdminGoodsListItemVo(
                entity.getId(),
                entity.getTitle(),
                entity.getPrice(),
                categoryName(category),
                displayName(seller),
                defaultLong(entity.getFavoriteCount()),
                entity.getStatus(),
                DateTimeUtil.formatDateTime(resolvePublishedAt(entity.getPublishedAt(), entity.getCreateTime())));
    }

    private AdminGoodsDetailVo toAdminGoodsDetailVo(GoodsEntity entity, UserEntity seller, CategoryEntity category) {
        UserEntity resolvedSeller = seller != null ? seller : loadUsers(List.of(entity.getUserId())).get(entity.getUserId());
        CategoryEntity resolvedCategory = category != null ? category : loadCategories(List.of(entity.getCategoryId())).get(entity.getCategoryId());
        return new AdminGoodsDetailVo(
                entity.getId(),
                entity.getTitle(),
                entity.getPrice(),
                entity.getOriginalPrice(),
                categoryName(resolvedCategory),
                entity.getCampus(),
                entity.getConditionLevel(),
                displayName(resolvedSeller),
                entity.getIntro(),
                entity.getDescription(),
                readStringList(entity.getTagsJson()),
                defaultLong(entity.getFavoriteCount()),
                entity.getStatus(),
                DateTimeUtil.formatDateTime(resolvePublishedAt(entity.getPublishedAt(), entity.getCreateTime())));
    }

    private List<WantedVo> toWantedVoList(List<WantedEntity> wantedList, boolean loggedIn) {
        if (wantedList.isEmpty()) {
            return List.of();
        }
        Map<Long, UserEntity> users = loadUsers(wantedList.stream().map(WantedEntity::getUserId).toList());
        Map<Long, CategoryEntity> categories = loadCategories(wantedList.stream().map(WantedEntity::getCategoryId).toList());
        return wantedList.stream()
                .map(item -> toWantedVo(item, loggedIn, users.get(item.getUserId()), categories.get(item.getCategoryId())))
                .toList();
    }

    private WantedVo toWantedVo(WantedEntity entity, boolean loggedIn, UserEntity publisher, CategoryEntity category) {
        UserEntity resolvedPublisher = publisher != null ? publisher : loadUsers(List.of(entity.getUserId())).get(entity.getUserId());
        CategoryEntity resolvedCategory = category != null ? category : loadCategories(List.of(entity.getCategoryId())).get(entity.getCategoryId());
        List<String> imageUrls = readStringList(entity.getImageUrlsJson());
        String coverImageUrl = imageUrls.isEmpty() ? "" : imageUrls.get(0);
        return new WantedVo(
                entity.getId(),
                entity.getTitle(),
                entity.getBudget(),
                categoryName(resolvedCategory),
                entity.getCampus(),
                displayName(resolvedPublisher),
                DateTimeUtil.formatDate(entity.getDeadline()),
                entity.getIntro(),
                entity.getDescription(),
                readStringList(entity.getTagsJson()),
                imageUrls,
                coverImageUrl,
                StringUtils.hasText(entity.getCoverStyle()) ? entity.getCoverStyle() : generateCoverStyle(categoryName(resolvedCategory), entity.getTitle()),
                loggedIn,
                loggedIn ? phoneOf(resolvedPublisher) : "",
                loggedIn ? qqOf(resolvedPublisher) : "",
                entity.getStatus());
    }

    private List<AppointmentVo> toAppointmentVoList(List<AppointmentEntity> appointments) {
        if (appointments.isEmpty()) {
            return List.of();
        }
        Map<Long, GoodsEntity> goods = loadGoodsMap(appointments.stream().map(AppointmentEntity::getGoodsId).toList());
        Set<Long> userIds = new HashSet<>();
        appointments.forEach(item -> {
            userIds.add(item.getBuyerId());
            userIds.add(item.getSellerId());
        });
        Map<Long, UserEntity> users = loadUsers(userIds);
        return appointments.stream()
                .map(item -> toAppointmentVo(item, goods.get(item.getGoodsId()), users.get(item.getBuyerId()), users.get(item.getSellerId())))
                .toList();
    }

    private AppointmentVo toAppointmentVo(AppointmentEntity entity, GoodsEntity goods, UserEntity buyer, UserEntity seller) {
        GoodsEntity resolvedGoods = goods != null ? goods : loadGoodsMap(List.of(entity.getGoodsId())).get(entity.getGoodsId());
        UserEntity resolvedBuyer = buyer != null ? buyer : loadUsers(List.of(entity.getBuyerId())).get(entity.getBuyerId());
        UserEntity resolvedSeller = seller != null ? seller : loadUsers(List.of(entity.getSellerId())).get(entity.getSellerId());
        return new AppointmentVo(
                entity.getId(),
                entity.getGoodsId(),
                resolvedGoods == null ? "" : resolvedGoods.getTitle(),
                entity.getBuyerId(),
                displayName(resolvedBuyer),
                entity.getSellerId(),
                displayName(resolvedSeller),
                DateTimeUtil.formatDateTime(entity.getIntendedTime()),
                entity.getIntendedLocation(),
                entity.getRemark(),
                entity.getStatus());
    }

    private AnnouncementVo toAnnouncementVo(AnnouncementEntity entity) {
        return new AnnouncementVo(entity.getId(), entity.getTitle(), entity.getSummary(), entity.getContent(), DateTimeUtil.formatDateTime(entity.getPublishedAt()), entity.getLevel());
    }

    private NotificationVo toNotificationVo(NotificationEntity entity) {
        return new NotificationVo(entity.getId(), entity.getTitle(), entity.getContent(), entity.getType(), Boolean.TRUE.equals(entity.getIsRead()), entity.getRelatedId(), DateTimeUtil.formatDateTime(entity.getCreateTime()));
    }

    private AdminAnnouncementListItemVo toAdminAnnouncementListItem(AnnouncementEntity entity) {
        return new AdminAnnouncementListItemVo(entity.getId(), entity.getTitle(), entity.getSummary(), entity.getLevel(), Boolean.TRUE.equals(entity.getTop()), Boolean.TRUE.equals(entity.getPublished()), DateTimeUtil.formatDateTime(entity.getPublishedAt()));
    }

    private AdminAnnouncementDetailVo toAdminAnnouncementDetailVo(AnnouncementEntity entity) {
        return new AdminAnnouncementDetailVo(entity.getId(), entity.getTitle(), entity.getSummary(), entity.getContent(), entity.getLevel(), Boolean.TRUE.equals(entity.getTop()), Boolean.TRUE.equals(entity.getPublished()), DateTimeUtil.formatDateTime(entity.getPublishedAt()));
    }

    private boolean matchesKeyword(String keyword, String... values) {
        if (!StringUtils.hasText(keyword)) {
            return true;
        }
        String normalized = keyword.trim().toLowerCase(Locale.ROOT);
        for (String value : values) {
            if (value != null && value.toLowerCase(Locale.ROOT).contains(normalized)) {
                return true;
            }
        }
        return false;
    }

    private <T> PageResult<T> toPageResult(List<T> allRecords, Long pageNum, Long pageSize) {
        long safePageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        long safePageSize = pageSize == null || pageSize < 1 ? 10 : pageSize;
        int fromIndex = (int) Math.min((safePageNum - 1) * safePageSize, allRecords.size());
        int toIndex = (int) Math.min(fromIndex + safePageSize, allRecords.size());
        return new PageResult<>(allRecords.subList(fromIndex, toIndex), allRecords.size(), safePageNum, safePageSize);
    }

    private void addNotification(Long userId, String title, String content, String type, Long relatedId) {
        NotificationEntity entity = new NotificationEntity();
        entity.setUserId(userId);
        entity.setTitle(title);
        entity.setContent(content);
        entity.setType(type);
        entity.setIsRead(Boolean.FALSE);
        entity.setRelatedId(relatedId);
        notificationMapper.insert(entity);
    }

    private Set<Long> loadFavoriteGoodsIds(Long userId) {
        if (userId == null) {
            return Set.of();
        }
        return favoriteMapper.selectList(Wrappers.<FavoriteEntity>lambdaQuery().eq(FavoriteEntity::getUserId, userId))
                .stream()
                .map(FavoriteEntity::getGoodsId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Map<Long, UserEntity> loadUsers(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        return userMapper.selectBatchIds(distinctIds(ids)).stream().collect(Collectors.toMap(UserEntity::getId, item -> item));
    }

    private Map<Long, CategoryEntity> loadCategories(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        return categoryMapper.selectBatchIds(distinctIds(ids)).stream().collect(Collectors.toMap(CategoryEntity::getId, item -> item));
    }

    private Map<Long, GoodsEntity> loadGoodsMap(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        return goodsMapper.selectBatchIds(distinctIds(ids)).stream().collect(Collectors.toMap(GoodsEntity::getId, item -> item));
    }

    private List<Long> distinctIds(Collection<Long> ids) {
        return ids.stream().filter(Objects::nonNull).distinct().toList();
    }

    private String writeStringList(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values == null ? List.of() : values);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize JSON", ex);
        }
    }

    private List<String> readStringList(String json) {
        if (!StringUtils.hasText(json)) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, STRING_LIST_TYPE);
        } catch (JsonProcessingException ex) {
            return List.of();
        }
    }

    private String generateUniqueStudentNo() {
        for (int i = 0; i < 10; i++) {
            String candidate = "KC" + (System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(1000));
            Long count = userMapper.selectCount(Wrappers.<UserEntity>lambdaQuery().eq(UserEntity::getStudentNo, candidate));
            if (count == null || count == 0) {
                return candidate;
            }
        }
        throw new IllegalStateException("Failed to generate student number");
    }

    private String generateUniquePhone() {
        for (int i = 0; i < 10; i++) {
            long value = Math.abs(System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(100000));
            String candidate = "13" + String.format("%09d", value % 1_000_000_000L);
            Long count = userMapper.selectCount(Wrappers.<UserEntity>lambdaQuery().eq(UserEntity::getPhone, candidate));
            if (count == null || count == 0) {
                return candidate;
            }
        }
        throw new IllegalStateException("Failed to generate phone");
    }

    private <T> List<T> limit(List<T> values, int size) {
        if (values.size() <= size) {
            return values;
        }
        return values.subList(0, size);
    }

    private boolean isDisabled(UserEntity entity) {
        return "disabled".equalsIgnoreCase(entity.getStatus());
    }

    private String displayName(UserEntity entity) {
        return entity == null ? "" : normalizeOptionalText(entity.getRealName());
    }

    private String phoneOf(UserEntity entity) {
        return entity == null ? "" : normalizeOptionalText(entity.getPhone());
    }

    private String qqOf(UserEntity entity) {
        return entity == null ? "" : normalizeOptionalText(entity.getQq());
    }

    private String categoryName(CategoryEntity entity) {
        return entity == null ? "" : normalizeOptionalText(entity.getName());
    }

    private long safeCount(Long value) {
        return value == null ? 0L : value;
    }

    private Long defaultLong(Long value) {
        return value == null ? 0L : value;
    }

    private LocalDateTime resolvePublishedAt(LocalDateTime primary, LocalDateTime fallback) {
        return primary != null ? primary : fallback;
    }
}
