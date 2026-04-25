package com.kecheng.market.announcement.service.impl;

import com.kecheng.market.announcement.service.AnnouncementService;
import com.kecheng.market.announcement.vo.AnnouncementVo;
import com.kecheng.market.common.store.MarketPersistenceService;
import com.kecheng.market.common.store.MarketStore;
import com.kecheng.market.common.store.StorageAccessSupport;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AnnouncementServiceImpl implements AnnouncementService {

    private final StorageAccessSupport storage;

    public AnnouncementServiceImpl(
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
    public List<AnnouncementVo> list() {
        return storage.route(
                () -> storage.mysqlStore().listAnnouncements(),
                () -> storage.memoryStore().listAnnouncements()
        );
    }

    @Override
    public AnnouncementVo detail(Long id) {
        return storage.route(
                () -> storage.mysqlStore().getAnnouncementDetail(id),
                () -> storage.memoryStore().getAnnouncementDetail(id)
        );
    }
}
