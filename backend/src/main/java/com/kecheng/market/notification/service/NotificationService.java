package com.kecheng.market.notification.service;

import com.kecheng.market.notification.vo.NotificationVo;
import java.util.List;

public interface NotificationService {
    List<NotificationVo> list(Long userId);
    long unreadCount(Long userId);
    void markRead(Long userId, Long id);
    void markAllRead(Long userId);
}
