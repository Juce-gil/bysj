package com.kecheng.market.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserProfileRequest(
        @NotBlank(message = "Name cannot be blank")
        @Size(max = 50, message = "Name length must be at most 50 characters")
        String name,

        @NotBlank(message = "Phone cannot be blank")
        @Size(max = 20, message = "Phone length must be at most 20 characters")
        String phone,

        @Size(max = 20, message = "QQ length must be at most 20 characters")
        String qq,

        @Size(max = 120, message = "Slogan length must be at most 120 characters")
        String slogan
) {
}