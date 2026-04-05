package com.kecheng.market.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 通用分页查询参数。
 */
public class PageQuery {

    @Schema(description = "页码", example = "1", defaultValue = "1")
    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码最小为 1")
    private Long pageNum = 1L;

    @Schema(description = "每页条数", example = "10", defaultValue = "10")
    @NotNull(message = "分页大小不能为空")
    @Min(value = 1, message = "分页大小最小为 1")
    @Max(value = 100, message = "分页大小最大为 100")
    private Long pageSize = 10L;

    public Long getPageNum() {
        return pageNum;
    }

    public void setPageNum(Long pageNum) {
        this.pageNum = pageNum;
    }

    public Long getPageSize() {
        return pageSize;
    }

    public void setPageSize(Long pageSize) {
        this.pageSize = pageSize;
    }
}
