package com.kecheng.market.goods.service.impl;

import com.kecheng.market.common.store.MarketStore;
import com.kecheng.market.goods.service.GoodsService;
import com.kecheng.market.goods.vo.GoodsVo;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class GoodsServiceImpl implements GoodsService {

    private final MarketStore marketStore;

    public GoodsServiceImpl(MarketStore marketStore) {
        this.marketStore = marketStore;
    }

    @Override
    public List<GoodsVo> list(Long userId) {
        return marketStore.listGoods(userId);
    }

    @Override
    public List<GoodsVo> myGoods(Long userId) {
        return marketStore.listMyGoods(userId);
    }

    @Override
    public GoodsVo detail(Long id, Long userId) {
        return marketStore.getGoodsDetail(id, userId);
    }

    @Override
    public boolean toggleFavorite(Long userId, Long goodsId) {
        return marketStore.toggleFavorite(userId, goodsId);
    }

    @Override
    public List<GoodsVo> myFavorites(Long userId) {
        return marketStore.listMyFavorites(userId);
    }

    @Override
    public GoodsVo create(Long userId, String title, BigDecimal price, BigDecimal originalPrice, String category, String campus,
                          String condition, String intro, String description, List<String> tags, List<String> imageUrls) {
        return marketStore.createGoods(userId, title, price, originalPrice, category, campus, condition, intro, description, tags, imageUrls);
    }

    @Override
    public GoodsVo update(Long userId, Long goodsId, String title, BigDecimal price, BigDecimal originalPrice, String category,
                          String campus, String condition, String intro, String description, List<String> tags, List<String> imageUrls) {
        return marketStore.updateGoods(userId, goodsId, title, price, originalPrice, category, campus, condition, intro, description, tags, imageUrls);
    }

    @Override
    public GoodsVo offShelf(Long userId, Long goodsId) {
        return marketStore.offShelfGoods(userId, goodsId);
    }

    @Override
    public GoodsVo relist(Long userId, Long goodsId) {
        return marketStore.relistGoods(userId, goodsId);
    }

    @Override
    public void delete(Long userId, Long goodsId) {
        marketStore.deleteGoods(userId, goodsId);
    }
}