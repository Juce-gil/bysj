package com.kecheng.market.home.controller;

import com.kecheng.market.common.ApiResponse;
import com.kecheng.market.home.service.HomeService;
import com.kecheng.market.home.vo.HomeDataVo;
import com.kecheng.market.security.annotation.AnonymousAccess;
import com.kecheng.market.security.context.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "首页模块")
@RestController
@RequestMapping("/api/home")
@AnonymousAccess
public class HomeController {

    private final HomeService homeService;

    public HomeController(HomeService homeService) {
        this.homeService = homeService;
    }

    @Operation(summary = "首页聚合数据")
    @GetMapping
    public ApiResponse<HomeDataVo> homeData() {
        return ApiResponse.success(homeService.homeData(UserContext.getUserId()));
    }
}
