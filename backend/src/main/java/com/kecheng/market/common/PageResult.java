package com.kecheng.market.common;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(name = "PageResult", description = "分页结果")
public record PageResult<T>(
        @Schema(description = "当前页数据") List<T> records,
        @Schema(description = "总记录数", example = "12") long total,
        @Schema(description = "页码", example = "1") long pageNum,
        @Schema(description = "每页条数", example = "10") long pageSize
) {
}
