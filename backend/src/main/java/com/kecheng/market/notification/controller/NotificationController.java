package com.kecheng.market.notification.controller;

import com.kecheng.market.common.ApiResponse;
import com.kecheng.market.notification.service.NotificationService;
import com.kecheng.market.notification.vo.NotificationVo;
import com.kecheng.market.security.annotation.RequireLogin;
import com.kecheng.market.security.context.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "通知模块")
@RestController
@RequestMapping("/api/notifications")
@RequireLogin
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Operation(summary = "通知列表")
    @GetMapping
    public ApiResponse<List<NotificationVo>> list() {
        return ApiResponse.success(notificationService.list(UserContext.getUserId()));
    }

    @Operation(summary = "未读通知数量")
    @GetMapping("/unread-count")
    public ApiResponse<Long> unreadCount() {
        return ApiResponse.success(notificationService.unreadCount(UserContext.getUserId()));
    }

    @Operation(summary = "标记通知已读")
    @PutMapping("/{id}/read")
    public ApiResponse<Boolean> markRead(@PathVariable Long id) {
        notificationService.markRead(UserContext.getUserId(), id);
        return ApiResponse.success(true);
    }

    @Operation(summary = "全部标记为已读")
    @PutMapping("/read-all")
    public ApiResponse<Boolean> markAllRead() {
        notificationService.markAllRead(UserContext.getUserId());
        return ApiResponse.success(true);
    }
}
