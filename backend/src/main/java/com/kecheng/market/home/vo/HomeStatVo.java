package com.kecheng.market.home.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "首页统计项")
public record HomeStatVo(
        @Schema(description = "展示名称", example = "在售闲置") String label,
        @Schema(description = "展示值", example = "08") String value
) {
}
