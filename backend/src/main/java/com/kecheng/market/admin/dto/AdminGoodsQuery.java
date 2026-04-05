package com.kecheng.market.admin.dto;

import com.kecheng.market.common.dto.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 后台商品分页查询参数。
 */
public class AdminGoodsQuery extends PageQuery {

    @Schema(description = "关键字：匹配商品标题、卖家名称、分类")
    private String keyword;

    @Schema(description = "商品状态", example = "on_sale")
    private String status;

    @Schema(description = "商品分类", example = "数码产品")
    private String category;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}