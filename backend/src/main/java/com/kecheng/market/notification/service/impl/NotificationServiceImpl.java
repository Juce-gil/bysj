package com.kecheng.market.notification.service.impl;

import com.kecheng.market.common.store.MarketPersistenceService;
import com.kecheng.market.common.store.MarketStore;
import com.kecheng.market.notification.service.NotificationService;
import com.kecheng.market.notification.vo.NotificationVo;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final String storageMode;
    private final MarketStore marketStore;
    private final MarketPersistenceService persistenceService;

    public NotificationServiceImpl(
            @Value("${market.storage-mode:mysql}") String storageMode,
            ObjectProvider<MarketStore> marketStoreProvider,
            ObjectProvider<MarketPersistenceService> persistenceServiceProvider) {
        this.storageMode = storageMode;
        this.marketStore = marketStoreProvider.getIfAvailable();
        this.persistenceService = persistenceServiceProvider.getIfAvailable();
    }

    @Override
    public List<NotificationVo> list(Long userId) {
        return useMysql() ? requirePersistence().listNotifications(userId) : requireStore().listNotifications(userId);
    }

    @Override
    public long unreadCount(Long userId) {
        return useMysql() ? requirePersistence().unreadNotificationCount(userId) : requireStore().unreadNotificationCount(userId);
    }

    @Override
    public void markRead(Long userId, Long id) {
        if (useMysql()) {
            requirePersistence().markNotificationRead(userId, id);
            return;
        }
        requireStore().markNotificationRead(userId, id);
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
