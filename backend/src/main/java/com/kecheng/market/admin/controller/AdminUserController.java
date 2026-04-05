package com.kecheng.market.admin.controller;

import com.kecheng.market.admin.dto.AdminUserQuery;
import com.kecheng.market.admin.dto.AdminUserStatusUpdateRequest;
import com.kecheng.market.admin.service.AdminUserService;
import com.kecheng.market.admin.vo.AdminUserListItemVo;
import com.kecheng.market.common.ApiResponse;
import com.kecheng.market.common.PageResult;
import com.kecheng.market.security.annotation.RequireAdmin;
import com.kecheng.market.security.context.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin User Management")
@RestController
@RequestMapping("/api/admin/users")
@RequireAdmin
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @Operation(summary = "Page admin users")
    @GetMapping
    public ApiResponse<PageResult<AdminUserListItemVo>> page(@Valid @ModelAttribute AdminUserQuery query) {
        return ApiResponse.success(adminUserService.page(query));
    }

    @Operation(summary = "Update user disabled status")
    @PutMapping("/{id}/status")
    public ApiResponse<AdminUserListItemVo> updateStatus(@PathVariable Long id, @Valid @RequestBody AdminUserStatusUpdateRequest request) {
        return ApiResponse.success(adminUserService.updateStatus(UserContext.getUserId(), id, request.disabled()));
    }
}