package com.kecheng.market.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Admin goods detail")
public record AdminGoodsDetailVo(
        @Schema(description = "Goods id") Long id,
        @Schema(description = "Goods title") String title,
        @Schema(description = "Goods price") BigDecimal price,
        @Schema(description = "Goods original price") BigDecimal originalPrice,
        @Schema(description = "Goods category") String category,
        @Schema(description = "Goods campus") String campus,
        @Schema(description = "Goods condition") String condition,
        @Schema(description = "Seller name") String sellerName,
        @Schema(description = "Goods introduction") String intro,
        @Schema(description = "Goods description") String description,
        @Schema(description = "Goods tags") List<String> tags,
        @Schema(description = "Favorite count") Long favoriteCount,
        @Schema(description = "Goods status") String status,
        @Schema(description = "Goods published time") String publishedAt
) {
}
