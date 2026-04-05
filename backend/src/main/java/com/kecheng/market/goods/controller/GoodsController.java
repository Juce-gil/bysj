package com.kecheng.market.goods.controller;

import com.kecheng.market.common.ApiResponse;
import com.kecheng.market.goods.dto.CreateGoodsRequest;
import com.kecheng.market.goods.service.GoodsService;
import com.kecheng.market.goods.vo.GoodsVo;
import com.kecheng.market.security.annotation.AnonymousAccess;
import com.kecheng.market.security.annotation.RequireLogin;
import com.kecheng.market.security.context.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Goods")
@RestController
@RequestMapping("/api/goods")
public class GoodsController {

    private final GoodsService goodsService;

    public GoodsController(GoodsService goodsService) {
        this.goodsService = goodsService;
    }

    @Operation(summary = "List goods")
    @GetMapping
    @AnonymousAccess
    public ApiResponse<List<GoodsVo>> list() {
        return ApiResponse.success(goodsService.list(UserContext.getUserId()));
    }

    @Operation(summary = "Goods detail")
    @GetMapping("/{id}")
    @AnonymousAccess
    public ApiResponse<GoodsVo> detail(@PathVariable Long id) {
        return ApiResponse.success(goodsService.detail(id, UserContext.getUserId()));
    }

    @Operation(summary = "Create goods")
    @PostMapping
    @RequireLogin
    public ApiResponse<GoodsVo> create(@Valid @RequestBody CreateGoodsRequest request) {
        return ApiResponse.success(goodsService.create(
                UserContext.getUserId(),
                request.title(),
                request.price(),
                request.originalPrice(),
                request.category(),
                request.campus(),
                request.condition(),
                request.intro(),
                request.description(),
                request.tags(),
                request.imageUrls()
        ));
    }

    @Operation(summary = "Update goods")
    @PutMapping("/{id}")
    @RequireLogin
    public ApiResponse<GoodsVo> update(@PathVariable Long id, @Valid @RequestBody CreateGoodsRequest request) {
        return ApiResponse.success(goodsService.update(
                UserContext.getUserId(),
                id,
                request.title(),
                request.price(),
                request.originalPrice(),
                request.category(),
                request.campus(),
                request.condition(),
                request.intro(),
                request.description(),
                request.tags(),
                request.imageUrls()
        ));
    }

    @Operation(summary = "Off shelf goods")
    @PutMapping("/{id}/off-shelf")
    @RequireLogin
    public ApiResponse<GoodsVo> offShelf(@PathVariable Long id) {
        return ApiResponse.success(goodsService.offShelf(UserContext.getUserId(), id));
    }

    @Operation(summary = "Relist goods")
    @PutMapping("/{id}/relist")
    @RequireLogin
    public ApiResponse<GoodsVo> relist(@PathVariable Long id) {
        return ApiResponse.success(goodsService.relist(UserContext.getUserId(), id));
    }

    @Operation(summary = "Delete goods")
    @DeleteMapping("/{id}")
    @RequireLogin
    public ApiResponse<Boolean> delete(@PathVariable Long id) {
        goodsService.delete(UserContext.getUserId(), id);
        return ApiResponse.success(true);
    }

    @Operation(summary = "Toggle favorite on goods detail")
    @PostMapping("/{id}/favorite")
    @RequireLogin
    public ApiResponse<Boolean> toggleFavorite(@PathVariable Long id) {
        return ApiResponse.success(goodsService.toggleFavorite(UserContext.getUserId(), id));
    }

    @Operation(summary = "Current user favorite goods")
    @GetMapping("/favorites/me")
    @RequireLogin
    public ApiResponse<List<GoodsVo>> myFavorites() {
        return ApiResponse.success(goodsService.myFavorites(UserContext.getUserId()));
    }
}