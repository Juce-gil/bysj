package com.kecheng.market.banner.controller;

import com.kecheng.market.banner.service.BannerService;
import com.kecheng.market.banner.vo.BannerVo;
import com.kecheng.market.common.ApiResponse;
import com.kecheng.market.security.annotation.AnonymousAccess;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "轮播图模块")
@RestController
@RequestMapping("/api/banners")
@AnonymousAccess
public class BannerController {

    private final BannerService bannerService;

    public BannerController(BannerService bannerService) {
        this.bannerService = bannerService;
    }

    @Operation(summary = "轮播图列表")
    @GetMapping
    public ApiResponse<List<BannerVo>> list() {
        return ApiResponse.success(bannerService.list());
    }
}
