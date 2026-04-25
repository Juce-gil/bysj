package com.kecheng.market.admin.service.impl;

import com.kecheng.market.admin.dto.AdminAnnouncementQuery;
import com.kecheng.market.admin.dto.AdminAnnouncementSaveRequest;
import com.kecheng.market.admin.service.AdminAnnouncementService;
import com.kecheng.market.admin.vo.AdminAnnouncementDetailVo;
import com.kecheng.market.admin.vo.AdminAnnouncementListItemVo;
import com.kecheng.market.common.PageResult;
import com.kecheng.market.common.store.MarketPersistenceService;
import com.kecheng.market.common.store.MarketStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AdminAnnouncementServiceImpl implements AdminAnnouncementService {

    private final String storageMode;
    private final MarketStore marketStore;
    private final MarketPersistenceService persistenceService;

    public AdminAnnouncementServiceImpl(
            @Value("${market.storage-mode:mysql}") String storageMode,
            ObjectProvider<MarketStore> marketStoreProvider,
            ObjectProvider<MarketPersistenceService> persistenceServiceProvider) {
        this.storageMode = storageMode;
        this.marketStore = marketStoreProvider.getIfAvailable();
        this.persistenceService = persistenceServiceProvider.getIfAvailable();
    }

    @Override
    public PageResult<AdminAnnouncementListItemVo> page(AdminAnnouncementQuery query) {
        return useMysql()
                ? requirePersistence().pageAdminAnnouncements(query)
                : requireStore().pageAdminAnnouncements(query.getKeyword(), query.getPublished(), query.getPageNum(), query.getPageSize());
    }

    @Override
    public AdminAnnouncementDetailVo detail(Long id) {
        return useMysql() ? requirePersistence().getAdminAnnouncementDetail(id) : requireStore().getAdminAnnouncementDetail(id);
    }

    @Override
    public AdminAnnouncementDetailVo create(AdminAnnouncementSaveRequest request) {
        return useMysql()
                ? requirePersistence().createAdminAnnouncement(request)
                : requireStore().createAdminAnnouncement(request.title(), request.summary(), request.content(), request.level(), request.top(), request.published());
    }

    @Override
    public AdminAnnouncementDetailVo update(Long id, AdminAnnouncementSaveRequest request) {
        return useMysql()
                ? requirePersistence().updateAdminAnnouncement(id, request)
                : requireStore().updateAdminAnnouncement(id, request.title(), request.summary(), request.content(), request.level(), request.top(), request.published());
    }

    @Override
    public AdminAnnouncementDetailVo publish(Long id) {
        return useMysql() ? requirePersistence().publishAdminAnnouncement(id) : requireStore().publishAdminAnnouncement(id);
    }

    @Override
    public AdminAnnouncementDetailVo offline(Long id) {
        return useMysql() ? requirePersistence().offlineAdminAnnouncement(id) : requireStore().offlineAdminAnnouncement(id);
    }

    @Override
    public void delete(Long id) {
        if (useMysql()) {
            requirePersistence().deleteAdminAnnouncement(id);
            return;
        }
        requireStore().deleteAdminAnnouncement(id);
    }

    private boolean useMysql() {
        return "mysql".equalsIgnoreCase(storageMode);
    }

    private MarketPersistenceService requirePersistence() {
        if (persistenceService == null) {
            throw new IllegalStateException("MySQL persistence service is not available");
        }
        return persistenceService;
    }

    private MarketStore requireStore() {
        if (marketStore == null) {
            throw new IllegalStateException("Memory store is not available");
        }
        return marketStore;
    }
}
