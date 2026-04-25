package com.kecheng.market.admin.service.impl;

import com.kecheng.market.admin.dto.AdminAnnouncementQuery;
import com.kecheng.market.admin.dto.AdminAnnouncementSaveRequest;
import com.kecheng.market.admin.service.AdminAnnouncementService;
import com.kecheng.market.admin.vo.AdminAnnouncementDetailVo;
import com.kecheng.market.admin.vo.AdminAnnouncementListItemVo;
import com.kecheng.market.common.PageResult;
import com.kecheng.market.common.store.MarketPersistenceService;
import com.kecheng.market.common.store.MarketStore;
import com.kecheng.market.common.store.StorageAccessSupport;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AdminAnnouncementServiceImpl implements AdminAnnouncementService {

    private final StorageAccessSupport storage;

    public AdminAnnouncementServiceImpl(
            @Value("${market.storage-mode:mysql}") String storageMode,
            ObjectProvider<MarketStore> marketStoreProvider,
            ObjectProvider<MarketPersistenceService> persistenceServiceProvider) {
        this.storage = new StorageAccessSupport(
                storageMode,
                marketStoreProvider.getIfAvailable(),
                persistenceServiceProvider.getIfAvailable()
        );
    }

    @Override
    public PageResult<AdminAnnouncementListItemVo> page(AdminAnnouncementQuery query) {
        return storage.route(
                () -> storage.mysqlStore().pageAdminAnnouncements(query),
                () -> storage.memoryStore().pageAdminAnnouncements(query.getKeyword(), query.getPublished(), query.getPageNum(), query.getPageSize())
        );
    }

    @Override
    public AdminAnnouncementDetailVo detail(Long id) {
        return storage.route(
                () -> storage.mysqlStore().getAdminAnnouncementDetail(id),
                () -> storage.memoryStore().getAdminAnnouncementDetail(id)
        );
    }

    @Override
    public AdminAnnouncementDetailVo create(AdminAnnouncementSaveRequest request) {
        return storage.route(
                () -> storage.mysqlStore().createAdminAnnouncement(request),
                () -> storage.memoryStore().createAdminAnnouncement(request.title(), request.summary(), request.content(), request.level(), request.top(), request.published())
        );
    }

    @Override
    public AdminAnnouncementDetailVo update(Long id, AdminAnnouncementSaveRequest request) {
        return storage.route(
                () -> storage.mysqlStore().updateAdminAnnouncement(id, request),
                () -> storage.memoryStore().updateAdminAnnouncement(id, request.title(), request.summary(), request.content(), request.level(), request.top(), request.published())
        );
    }

    @Override
    public AdminAnnouncementDetailVo publish(Long id) {
        return storage.route(
                () -> storage.mysqlStore().publishAdminAnnouncement(id),
                () -> storage.memoryStore().publishAdminAnnouncement(id)
        );
    }

    @Override
    public AdminAnnouncementDetailVo offline(Long id) {
        return storage.route(
                () -> storage.mysqlStore().offlineAdminAnnouncement(id),
                () -> storage.memoryStore().offlineAdminAnnouncement(id)
        );
    }

    @Override
    public void delete(Long id) {
        storage.route(
                () -> storage.mysqlStore().deleteAdminAnnouncement(id),
                () -> storage.memoryStore().deleteAdminAnnouncement(id)
        );
    }
}
