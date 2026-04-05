package com.kecheng.market.common;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ApiResponse", description = "统一响应体")
public record ApiResponse<T>(
        @Schema(description = "业务状态码", example = "200") Integer code,
        @Schema(description = "响应消息", example = "success") String message,
        @Schema(description = "响应数据") T data
) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "success", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data);
    }

    public static ApiResponse<Void> success() {
        return new ApiResponse<>(200, "success", null);
    }

    public static <T> ApiResponse<T> fail(Integer code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
