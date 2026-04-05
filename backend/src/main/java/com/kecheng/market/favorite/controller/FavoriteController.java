package com.kecheng.market.favorite.controller;

import com.kecheng.market.common.ApiResponse;
import com.kecheng.market.goods.service.GoodsService;
import com.kecheng.market.goods.vo.GoodsVo;
import com.kecheng.market.security.annotation.RequireLogin;
import com.kecheng.market.security.context.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "收藏模块")
@RestController
@RequestMapping("/api/favorites")
@RequireLogin
public class FavoriteController {

    private final GoodsService goodsService;

    public FavoriteController(GoodsService goodsService) {
        this.goodsService = goodsService;
    }

    @Operation(summary = "我的收藏列表")
    @GetMapping
    public ApiResponse<List<GoodsVo>> myFavorites() {
        return ApiResponse.success(goodsService.myFavorites(UserContext.getUserId()));
    }

    @Operation(summary = "切换商品收藏状态")
    @PostMapping("/{goodsId}/toggle")
    public ApiResponse<Boolean> toggle(@PathVariable Long goodsId) {
        return ApiResponse.success(goodsService.toggleFavorite(UserContext.getUserId(), goodsId));
    }
}
