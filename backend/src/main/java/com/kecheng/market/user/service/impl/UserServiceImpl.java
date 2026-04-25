package com.kecheng.market.user.service.impl;

import com.kecheng.market.common.store.MarketPersistenceService;
import com.kecheng.market.common.store.MarketStore;
import com.kecheng.market.user.service.UserService;
import com.kecheng.market.user.vo.UserProfileVo;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final String storageMode;
    private final MarketStore marketStore;
    private final MarketPersistenceService persistenceService;

    public UserServiceImpl(
            @Value("${market.storage-mode:mysql}") String storageMode,
            ObjectProvider<MarketStore> marketStoreProvider,
            ObjectProvider<MarketPersistenceService> persistenceServiceProvider) {
        this.storageMode = storageMode;
        this.marketStore = marketStoreProvider.getIfAvailable();
        this.persistenceService = persistenceServiceProvider.getIfAvailable();
    }

    @Override
    public UserProfileVo currentUser(Long userId) {
        return useMysql() ? requirePersistence().getUserProfile(userId) : requireStore().getUserProfile(userId);
    }

    @Override
    public UserProfileVo updateProfile(Long userId, String name, String phone, String qq, String slogan) {
        return useMysql()
                ? requirePersistence().updateUserProfile(userId, name, phone, qq, slogan)
                : requireStore().updateUserProfile(userId, name, phone, qq, slogan);
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
