package com.kecheng.market.admin.controller;

import com.kecheng.market.admin.dto.AdminGoodsQuery;
import com.kecheng.market.admin.service.AdminGoodsService;
import com.kecheng.market.admin.vo.AdminGoodsDetailVo;
import com.kecheng.market.admin.vo.AdminGoodsListItemVo;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Goods")
@RestController
@RequestMapping("/api/admin/goods")
@RequireAdmin
public class AdminGoodsController {

    private final AdminGoodsService adminGoodsService;

    public AdminGoodsController(AdminGoodsService adminGoodsService) {
        this.adminGoodsService = adminGoodsService;
    }

    @Operation(summary = "Admin goods page")
    @GetMapping
    public ApiResponse<PageResult<AdminGoodsListItemVo>> page(@Valid @ModelAttribute AdminGoodsQuery query) {
        return ApiResponse.success(adminGoodsService.page(query));
    }

    @Operation(summary = "Admin goods detail")
    @GetMapping("/{id}")
    public ApiResponse<AdminGoodsDetailVo> detail(@PathVariable Long id) {
        return ApiResponse.success(adminGoodsService.detail(id));
    }

    @Operation(summary = "Admin off shelf goods")
    @PutMapping("/{id}/off-shelf")
    public ApiResponse<AdminGoodsDetailVo> offShelf(@PathVariable Long id) {
        return ApiResponse.success("off_shelf", adminGoodsService.offShelf(id));
    }

    @Operation(summary = "Admin relist goods")
    @PutMapping("/{id}/relist")
    public ApiResponse<AdminGoodsDetailVo> relist(@PathVariable Long id) {
        return ApiResponse.success("relisted", adminGoodsService.relist(id));
    }

    @Operation(summary = "Admin delete goods")
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(@PathVariable Long id) {
        adminGoodsService.delete(id);
        return ApiResponse.success("deleted", true);
    }
}
