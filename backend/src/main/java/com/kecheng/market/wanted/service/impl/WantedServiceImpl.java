package com.kecheng.market.wanted.service.impl;

import com.kecheng.market.common.store.MarketPersistenceService;
import com.kecheng.market.common.store.MarketStore;
import com.kecheng.market.common.store.StorageAccessSupport;
import com.kecheng.market.wanted.service.WantedService;
import com.kecheng.market.wanted.vo.WantedVo;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WantedServiceImpl implements WantedService {

    private final StorageAccessSupport storage;

    public WantedServiceImpl(
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
    public List<WantedVo> list(Long userId) {
        return storage.route(
                () -> storage.mysqlStore().listWanted(userId),
                () -> storage.memoryStore().listWanted(userId)
        );
    }

    @Override
    public List<WantedVo> myWanted(Long userId) {
        return storage.route(
                () -> storage.mysqlStore().listMyWanted(userId),
                () -> storage.memoryStore().listMyWanted(userId)
        );
    }

    @Override
    public WantedVo detail(Long id, boolean loggedIn) {
        return storage.route(
                () -> storage.mysqlStore().getWantedDetail(id, loggedIn),
                () -> storage.memoryStore().getWantedDetail(id, loggedIn)
        );
    }

    @Override
    public WantedVo create(Long userId, String title, String budget, String category, String campus, String deadline,
                           String intro, String description, List<String> tags, List<String> imageUrls) {
        return storage.route(
                () -> storage.mysqlStore().createWanted(userId, title, budget, category, campus, deadline, intro, description, tags, imageUrls),
                () -> storage.memoryStore().createWanted(userId, title, budget, category, campus, deadline, intro, description, tags, imageUrls)
        );
    }

    @Override
    public WantedVo update(Long userId, Long wantedId, String title, String budget, String category, String campus, String deadline,
                           String intro, String description, List<String> tags, List<String> imageUrls) {
        return storage.route(
                () -> storage.mysqlStore().updateWanted(userId, wantedId, title, budget, category, campus, deadline, intro, description, tags, imageUrls),
                () -> storage.memoryStore().updateWanted(userId, wantedId, title, budget, category, campus, deadline, intro, description, tags, imageUrls)
        );
    }

    @Override
    public WantedVo close(Long userId, Long wantedId) {
        return storage.route(
                () -> storage.mysqlStore().closeWanted(userId, wantedId),
                () -> storage.memoryStore().closeWanted(userId, wantedId)
        );
    }

    @Override
    public WantedVo reopen(Long userId, Long wantedId) {
        return storage.route(
                () -> storage.mysqlStore().reopenWanted(userId, wantedId),
                () -> storage.memoryStore().reopenWanted(userId, wantedId)
        );
    }

    @Override
    public void delete(Long userId, Long wantedId) {
        storage.route(
                () -> storage.mysqlStore().deleteWanted(userId, wantedId),
                () -> storage.memoryStore().deleteWanted(userId, wantedId)
        );
    }
}
