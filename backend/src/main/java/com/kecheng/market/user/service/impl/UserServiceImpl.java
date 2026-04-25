package com.kecheng.market.user.service.impl;

import com.kecheng.market.common.store.MarketPersistenceService;
import com.kecheng.market.common.store.MarketStore;
import com.kecheng.market.common.store.StorageAccessSupport;
import com.kecheng.market.user.service.UserService;
import com.kecheng.market.user.vo.UserProfileVo;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final StorageAccessSupport storage;

    public UserServiceImpl(
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
    public UserProfileVo currentUser(Long userId) {
        return storage.route(
                () -> storage.mysqlStore().getUserProfile(userId),
                () -> storage.memoryStore().getUserProfile(userId)
        );
    }

    @Override
    public UserProfileVo updateProfile(Long userId, String name, String phone, String qq, String slogan) {
        return storage.route(
                () -> storage.mysqlStore().updateUserProfile(userId, name, phone, qq, slogan),
                () -> storage.memoryStore().updateUserProfile(userId, name, phone, qq, slogan)
        );
    }
}
