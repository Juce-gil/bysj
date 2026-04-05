package com.kecheng.market.announcement.controller;

import com.kecheng.market.announcement.service.AnnouncementService;
import com.kecheng.market.announcement.vo.AnnouncementVo;
import com.kecheng.market.common.ApiResponse;
import com.kecheng.market.security.annotation.AnonymousAccess;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "公告模块")
@RestController
@RequestMapping("/api/announcements")
@AnonymousAccess
public class AnnouncementController {

    private final AnnouncementService announcementService;

    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @Operation(summary = "公告列表")
    @GetMapping
    public ApiResponse<List<AnnouncementVo>> list() {
        return ApiResponse.success(announcementService.list());
    }

    @Operation(summary = "公告详情")
    @GetMapping("/{id}")
    public ApiResponse<AnnouncementVo> detail(@PathVariable Long id) {
        return ApiResponse.success(announcementService.detail(id));
    }
}
