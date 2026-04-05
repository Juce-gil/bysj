package com.kecheng.market.admin.dto;

import jakarta.validation.constraints.NotNull;

public record AdminUserStatusUpdateRequest(
        @NotNull(message = "Disabled cannot be null")
        Boolean disabled
) {
}