package com.kecheng.market.goods.service;

import com.kecheng.market.goods.vo.GoodsVo;
import java.math.BigDecimal;
import java.util.List;

public interface GoodsService {
    List<GoodsVo> list(Long userId);

    List<GoodsVo> myGoods(Long userId);

    GoodsVo detail(Long id, Long userId);

    boolean toggleFavorite(Long userId, Long goodsId);

    List<GoodsVo> myFavorites(Long userId);

    GoodsVo create(Long userId, String title, BigDecimal price, BigDecimal originalPrice, String category, String campus,
                   String condition, String intro, String description, List<String> tags, List<String> imageUrls);

    GoodsVo update(Long userId, Long goodsId, String title, BigDecimal price, BigDecimal originalPrice, String category,
                   String campus, String condition, String intro, String description, List<String> tags, List<String> imageUrls);

    GoodsVo offShelf(Long userId, Long goodsId);

    GoodsVo relist(Long userId, Long goodsId);

    void delete(Long userId, Long goodsId);
}