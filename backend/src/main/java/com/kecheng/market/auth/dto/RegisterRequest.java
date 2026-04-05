package com.kecheng.market.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @NotBlank(message = "用户名不能为空") String username,
        @NotBlank(message = "昵称不能为空") String nickname,
        @NotBlank(message = "密码不能为空") String password,
        @NotBlank(message = "确认密码不能为空") String confirmPassword
) {
}
