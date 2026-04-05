package com.kecheng.market.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "后台商品列表项")
public record AdminGoodsListItemVo(
        @Schema(description = "商品ID") Long id,
        @Schema(description = "标题") String title,
        @Schema(description = "价格") BigDecimal price,
        @Schema(description = "分类") String category,
        @Schema(description = "卖家名称") String sellerName,
        @Schema(description = "收藏数") Long favoriteCount,
        @Schema(description = "商品状态") String status,
        @Schema(description = "发布时间") String publishedAt
) {
}