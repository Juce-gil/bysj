package com.kecheng.market.wanted.service.impl;

import com.kecheng.market.common.store.MarketPersistenceService;
import com.kecheng.market.common.store.MarketStore;
import com.kecheng.market.wanted.service.WantedService;
import com.kecheng.market.wanted.vo.WantedVo;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WantedServiceImpl implements WantedService {

    private final String storageMode;
    private final MarketStore marketStore;
    private final MarketPersistenceService persistenceService;

    public WantedServiceImpl(
            @Value("${market.storage-mode:mysql}") String storageMode,
            ObjectProvider<MarketStore> marketStoreProvider,
            ObjectProvider<MarketPersistenceService> persistenceServiceProvider) {
        this.storageMode = storageMode;
        this.marketStore = marketStoreProvider.getIfAvailable();
        this.persistenceService = persistenceServiceProvider.getIfAvailable();
    }

    @Override
    public List<WantedVo> list(Long userId) {
        return useMysql() ? requirePersistence().listWanted(userId) : requireStore().listWanted(userId);
    }

    @Override
    public List<WantedVo> myWanted(Long userId) {
        return useMysql() ? requirePersistence().listMyWanted(userId) : requireStore().listMyWanted(userId);
    }

    @Override
    public WantedVo detail(Long id, boolean loggedIn) {
        return useMysql() ? requirePersistence().getWantedDetail(id, loggedIn) : requireStore().getWantedDetail(id, loggedIn);
    }

    @Override
    public WantedVo create(Long userId, String title, String budget, String category, String campus, String deadline,
                           String intro, String description, List<String> tags, List<String> imageUrls) {
        return useMysql()
                ? requirePersistence().createWanted(userId, title, budget, category, campus, deadline, intro, description, tags, imageUrls)
                : requireStore().createWanted(userId, title, budget, category, campus, deadline, intro, description, tags, imageUrls);
    }

    @Override
    public WantedVo update(Long userId, Long wantedId, String title, String budget, String category, String campus, String deadline,
                           String intro, String description, List<String> tags, List<String> imageUrls) {
        return useMysql()
                ? requirePersistence().updateWanted(userId, wantedId, title, budget, category, campus, deadline, intro, description, tags, imageUrls)
                : requireStore().updateWanted(userId, wantedId, title, budget, category, campus, deadline, intro, description, tags, imageUrls);
    }

    @Override
    public WantedVo close(Long userId, Long wantedId) {
        return useMysql() ? requirePersistence().closeWanted(userId, wantedId) : requireStore().closeWanted(userId, wantedId);
    }

    @Override
    public WantedVo reopen(Long userId, Long wantedId) {
        return useMysql() ? requirePersistence().reopenWanted(userId, wantedId) : requireStore().reopenWanted(userId, wantedId);
    }

    @Override
    public void delete(Long userId, Long wantedId) {
        if (useMysql()) {
            requirePersistence().deleteWanted(userId, wantedId);
            return;
        }
        requireStore().deleteWanted(userId, wantedId);
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
