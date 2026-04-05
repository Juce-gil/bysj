package com.kecheng.market.admin.controller;

import com.kecheng.market.admin.dto.AdminAnnouncementQuery;
import com.kecheng.market.admin.dto.AdminAnnouncementSaveRequest;
import com.kecheng.market.admin.service.AdminAnnouncementService;
import com.kecheng.market.admin.vo.AdminAnnouncementDetailVo;
import com.kecheng.market.admin.vo.AdminAnnouncementListItemVo;
import com.kecheng.market.common.ApiResponse;
import com.kecheng.market.common.PageResult;
import com.kecheng.market.security.annotation.RequireAdmin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Announcement")
@RestController
@RequestMapping("/api/admin/announcements")
@RequireAdmin
public class AdminAnnouncementController {

    private final AdminAnnouncementService adminAnnouncementService;

    public AdminAnnouncementController(AdminAnnouncementService adminAnnouncementService) {
        this.adminAnnouncementService = adminAnnouncementService;
    }

    @Operation(summary = "Admin announcement page")
    @GetMapping
    public ApiResponse<PageResult<AdminAnnouncementListItemVo>> page(@Valid @ModelAttribute AdminAnnouncementQuery query) {
        return ApiResponse.success(adminAnnouncementService.page(query));
    }

    @Operation(summary = "Admin announcement detail")
    @GetMapping("/{id}")
    public ApiResponse<AdminAnnouncementDetailVo> detail(@PathVariable Long id) {
        return ApiResponse.success(adminAnnouncementService.detail(id));
    }

    @Operation(summary = "Create announcement")
    @PostMapping
    public ApiResponse<AdminAnnouncementDetailVo> create(@Valid @RequestBody AdminAnnouncementSaveRequest request) {
        return ApiResponse.success("created", adminAnnouncementService.create(request));
    }

    @Operation(summary = "Update announcement")
    @PutMapping("/{id}")
    public ApiResponse<AdminAnnouncementDetailVo> update(@PathVariable Long id,
                                                          @Valid @RequestBody AdminAnnouncementSaveRequest request) {
        return ApiResponse.success("updated", adminAnnouncementService.update(id, request));
    }

    @Operation(summary = "Publish announcement")
    @PutMapping("/{id}/publish")
    public ApiResponse<AdminAnnouncementDetailVo> publish(@PathVariable Long id) {
        return ApiResponse.success("published", adminAnnouncementService.publish(id));
    }

    @Operation(summary = "Offline announcement")
    @PutMapping("/{id}/offline")
    public ApiResponse<AdminAnnouncementDetailVo> offline(@PathVariable Long id) {
        return ApiResponse.success("offline", adminAnnouncementService.offline(id));
    }

    @Operation(summary = "Delete announcement")
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(@PathVariable Long id) {
        adminAnnouncementService.delete(id);
        return ApiResponse.success("deleted", true);
    }
}
