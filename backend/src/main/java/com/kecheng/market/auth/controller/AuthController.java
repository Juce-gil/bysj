package com.kecheng.market.auth.controller;

import com.kecheng.market.auth.dto.LoginRequest;
import com.kecheng.market.auth.dto.RegisterRequest;
import com.kecheng.market.auth.service.AuthService;
import com.kecheng.market.auth.vo.LoginResponse;
import com.kecheng.market.common.ApiResponse;
import com.kecheng.market.security.annotation.AnonymousAccess;
import com.kecheng.market.user.vo.UserProfileVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "认证模块")
@RestController
@RequestMapping("/api/auth")
@AnonymousAccess
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "登录")
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @Operation(summary = "注册")
    @PostMapping("/register")
    public ApiResponse<UserProfileVo> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success("注册成功", authService.register(request));
    }
}
