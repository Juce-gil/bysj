package com.kecheng.market.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Admin user list item")
public record AdminUserListItemVo(
        @Schema(description = "User ID") Long id,
        @Schema(description = "Username") String username,
        @Schema(description = "Display name") String displayName,
        @Schema(description = "Role") String role,
        @Schema(description = "Student number") String studentNo,
        @Schema(description = "Phone") String phone,
        @Schema(description = "School") String school,
        @Schema(description = "Disabled") Boolean disabled,
        @Schema(description = "Register time") String registerTime
) {
}