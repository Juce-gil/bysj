package com.kecheng.market.user.controller;

import com.kecheng.market.common.ApiResponse;
import com.kecheng.market.goods.service.GoodsService;
import com.kecheng.market.goods.vo.GoodsVo;
import com.kecheng.market.security.annotation.RequireLogin;
import com.kecheng.market.security.context.UserContext;
import com.kecheng.market.user.dto.UpdateUserProfileRequest;
import com.kecheng.market.user.service.UserService;
import com.kecheng.market.user.vo.UserProfileVo;
import com.kecheng.market.wanted.service.WantedService;
import com.kecheng.market.wanted.vo.WantedVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User Center")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final GoodsService goodsService;
    private final WantedService wantedService;

    public UserController(UserService userService, GoodsService goodsService, WantedService wantedService) {
        this.userService = userService;
        this.goodsService = goodsService;
        this.wantedService = wantedService;
    }

    @Operation(summary = "Get current user profile")
    @GetMapping("/me")
    @RequireLogin
    public ApiResponse<UserProfileVo> currentUser() {
        return ApiResponse.success(userService.currentUser(UserContext.getUserId()));
    }

    @Operation(summary = "Update current user profile")
    @PutMapping("/me")
    @RequireLogin
    public ApiResponse<UserProfileVo> updateProfile(@Valid @RequestBody UpdateUserProfileRequest request) {
        return ApiResponse.success(userService.updateProfile(
                UserContext.getUserId(),
                request.name(),
                request.phone(),
                request.qq(),
                request.slogan()
        ));
    }

    @Operation(summary = "Get goods posted by current user")
    @GetMapping("/me/goods")
    @RequireLogin
    public ApiResponse<List<GoodsVo>> myGoods() {
        return ApiResponse.success(goodsService.myGoods(UserContext.getUserId()));
    }

    @Operation(summary = "Get wanted posts by current user")
    @GetMapping("/me/wanted")
    @RequireLogin
    public ApiResponse<List<WantedVo>> myWanted() {
        return ApiResponse.success(wantedService.myWanted(UserContext.getUserId()));
    }
}