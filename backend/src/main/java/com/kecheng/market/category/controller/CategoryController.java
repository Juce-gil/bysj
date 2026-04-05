package com.kecheng.market.category.controller;

import com.kecheng.market.category.service.CategoryService;
import com.kecheng.market.category.vo.CategoryVo;
import com.kecheng.market.common.ApiResponse;
import com.kecheng.market.security.annotation.AnonymousAccess;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "分类模块")
@RestController
@RequestMapping("/api/categories")
@AnonymousAccess
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(summary = "分类列表")
    @GetMapping
    public ApiResponse<List<CategoryVo>> list() {
        return ApiResponse.success(categoryService.list());
    }
}
