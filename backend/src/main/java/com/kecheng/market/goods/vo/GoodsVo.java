package com.kecheng.market.goods.vo;

import java.math.BigDecimal;
import java.util.List;

public record GoodsVo(Long id, Long sellerId, String title, BigDecimal price, BigDecimal originalPrice, String category, String campus,
                      String condition, String seller, String publishedAt, String intro, String description, List<String> tags,
                      List<String> imageUrls, String coverImageUrl, String coverStyle, Long favoriteCount, Boolean favorited,
                      String status) {
}
