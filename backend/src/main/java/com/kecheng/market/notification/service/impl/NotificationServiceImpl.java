package com.kecheng.market.notification.service.impl;

import com.kecheng.market.common.store.MarketPersistenceService;
import com.kecheng.market.common.store.MarketStore;
import com.kecheng.market.common.store.StorageAccessSupport;
import com.kecheng.market.notification.service.NotificationService;
import com.kecheng.market.notification.vo.NotificationVo;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final StorageAccessSupport storage;

    public NotificationServiceImpl(
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
    public List<NotificationVo> list(Long userId) {
        return storage.route(
                () -> storage.mysqlStore().listNotifications(userId),
                () -> storage.memoryStore().listNotifications(userId)
        );
    }

    @Override
    public long unreadCount(Long userId) {
        return storage.route(
                () -> storage.mysqlStore().unreadNotificationCount(userId),
                () -> storage.memoryStore().unreadNotificationCount(userId)
        );
    }

    @Override
    public void markRead(Long userId, Long id) {
        storage.route(
                () -> storage.mysqlStore().markNotificationRead(userId, id),
                () -> storage.memoryStore().markNotificationRead(userId, id)
        );
    }

    @Override
    public void markAllRead(Long userId) {
        storage.route(
                () -> storage.mysqlStore().markAllNotificationsRead(userId),
                () -> storage.memoryStore().markAllNotificationsRead(userId)
        );
    }
}
