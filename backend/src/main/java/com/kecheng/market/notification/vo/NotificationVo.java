package com.kecheng.market.notification.vo;

public record NotificationVo(Long id, String title, String content, String type, Boolean isRead, Long relatedId, String createTime) {
}
