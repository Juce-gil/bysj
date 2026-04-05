package com.kecheng.market.notification.service.impl;

import com.kecheng.market.common.store.MarketStore;
import com.kecheng.market.notification.service.NotificationService;
import com.kecheng.market.notification.vo.NotificationVo;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final MarketStore marketStore;

    public NotificationServiceImpl(MarketStore marketStore) {
        this.marketStore = marketStore;
    }

    @Override
    public List<NotificationVo> list(Long userId) {
        return marketStore.listNotifications(userId);
    }

    @Override
    public long unreadCount(Long userId) {
        return marketStore.unreadNotificationCount(userId);
    }

    @Override
    public void markRead(Long userId, Long id) {
        marketStore.markNotificationRead(userId, id);
    }
}
